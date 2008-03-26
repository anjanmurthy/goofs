#!/usr/bin/env python

import gdata.photos.service
import urllib2, httplib
import threading
import thread
import os
import datetime, time
import getpass
import getopt
import sys
from errno import *
from stat import *
import fcntl

# pull in some spaghetti to make this stuff work without fuse-py being installed
try:
    import _find_fuse_parts
except ImportError:
    pass
import fuse
from fuse import Fuse


if not hasattr(fuse, '__version__'):
    raise RuntimeError, \
        "your fuse-py doesn't know of fuse.__version__, probably it's too old."

fuse.fuse_python_api = (0, 2)

fuse.feature_assert('stateful_files', 'has_init')


HOME = None
GOOFS_CACHE = None
PHOTOS = None
PHOTOS_DIR = None
GDOCS_DIRS = None

def init(user):
	
    global HOME, GOOFS_CACHE, PHOTOS, PHOTOS_DIR, GDOCS_DIRS

    HOME = os.path.expanduser("~")
    GOOFS_CACHE = HOME + '/.goofs-cache/' + user.split('@')[0]
    PHOTOS = 'photos'
    PHOTOS_DIR = GOOFS_CACHE + '/' + PHOTOS
    GDOCS_DIRS = [PHOTOS_DIR]
    
    try:
        for root, dirs, files in os.walk(GOOFS_CACHE, topdown=False):
            for file in files:
                os.remove(os.path.join(root, file))
            for d in dirs:
                os.rmdir(os.path.join(root, d))
	
    except OSError, err:
        print 'removing did not work'
        print OSError
        print err

    try:
        for d in GDOCS_DIRS:
            os.makedirs(d)
    except OSError, err:
        print 'could not create the cache dirs'
        print OSError
        print err

class TaskThread(threading.Thread):
    """Thread that executes a task every N seconds"""
    
    def __init__(self):
        threading.Thread.__init__(self)
        self._finished = threading.Event()
        self._interval = 30.0
    
    def setInterval(self, interval):
        """Set the number of seconds we sleep between executing our task"""
        self._interval = interval
    
    def shutdown(self):
        """Stop this thread"""
        self._finished.set()
    
    def run(self):
	   while 1:
            if self._finished.isSet(): return
            self.task()    
            # sleep for interval or until shutdown
            self._finished.wait(self._interval)
    
    def task(self):
        """The task done by this thread - override in subclasses"""
        pass

class GPhotosService(gdata.photos.service.PhotosService):
	
	def __init__(self, email, password):
		gdata.photos.service.PhotosService.__init__(self, email, password)
	
	def GetAuthorizationToken(self):
		return self._GetAuthToken()
    
class GClient:
    
    def __init__(self, email, password):
        self.ph_client = GPhotosService(email, password)
        self.ph_client.ProgrammaticLogin()

    def albums_feed(self):
        return self.ph_client.GetUserFeed().entry

    def get_album_by_title(self, title):
        for album in self.albums_feed():
            if title == album.title.text:
                return album
        return None
    
    def delete_album_by_title(self, title):
         album = self.get_album_by_title(title)
         return self.ph_client.Delete(album)

    def photos_feed(self, album):
        return self.ph_client.GetFeed(album.GetPhotosUri()).entry

    def upload_photo(self, album, loc):
        return self.ph_client.InsertPhotoSimple(album, os.path.basename(loc), os.path.basename(loc), loc)

    def get_photo(self, uri):
        return self.ph_client.GetEntry(uri)
    
    def get_photo_by_title(self, title):
        for album in self.albums_feed():
            for photo in self.photos_feed(album):
                if title == photo.title.text:
                    return photo
        return None
    
    def delete_photo_by_title(self, title):
        photo = self.get_photo_by_title(title)
        return self.ph_client.Delete(photo)
    
	def delete_photo(self, uri):
		return self.ph_client.Delete(uri)

    def get_photo_content(self, photo):
		request = urllib2.Request(photo.media.content[0].url)
		request.add_header('Authorization', self.ph_client.GetAuthorizationToken())
		opener = urllib2.build_opener()
		f = opener.open(request)
		thecontent = f.read()
		f.close()
		return thecontent
    
    def get_photo_updated(self, photo):
		return datetime.datetime.strptime(self.get_photo_updated_str(photo)[0:19], '%Y-%m-%dT%H:%M:%S')

    def get_photo_updated_str(self, photo):
		return photo.updated.text
	
	
class GTaskThread(TaskThread):

    def __init__(self, client):
        TaskThread.__init__(self)
        self.client = client
            
    def task(self):
        for dir in GDOCS_DIRS:
            if dir == PHOTOS_DIR:
                album_feed = self.client.albums_feed()
            for album in album_feed:
                photos_feed = self.client.photos_feed(album)
                for photo in photos_feed:    
                    updated = self.client.get_photo_updated(photo)
                    out = None
                    try:
                        album_dir = dir + '/' + album.title.text
                        if not os.path.exists(album_dir):
                            os.mkdir(album_dir)
                        name = album_dir + '/' + photo.title.text
                        get = False						
                        if os.path.exists(name):
                            mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime = os.stat(name)
                            if updated > datetime.datetime.fromtimestamp(mtime):
                                get = True
                        else:
                            get = True
                        if get:
                            out = open(name, 'w')
                            out.write(self.client.get_photo_content(photo))
                            print 'wrote %s', name
                        else:
                            print 'no need to get'
                    finally:
						if out is not None:
							out.close()



def flag2mode(flags):
    md = {os.O_RDONLY: 'r', os.O_WRONLY: 'w', os.O_RDWR: 'w+'}
    m = md[flags & (os.O_RDONLY | os.O_WRONLY | os.O_RDWR)]

    if flags | os.O_APPEND:
        m = m.replace('w', 'a', 1)

    return m


class Goofs(Fuse):

    def __init__(self, user, pw, *args, **kw):
        Fuse.__init__(self, *args, **kw)
        self.root = GOOFS_CACHE
        self.user = user
        self.pw = pw
        self.client = GClient(self.user, self.pw)
	
    def getattr(self, path):
        return os.lstat("." + path)

    def readlink(self, path):
        return os.readlink("." + path)

    def readdir(self, path, offset):
        for e in os.listdir("." + path):
            yield fuse.Direntry(e)

    def unlink(self, path):
        self.client.delete_photo_by_title(os.path.basename(path))
        os.unlink("." + path)

    def rmdir(self, path):
        os.rmdir("." + path)

    def symlink(self, path, path1):
        os.symlink(path, "." + path1)

    def rename(self, path, path1):
        os.rename("." + path, "." + path1)

    def link(self, path, path1):
        os.link("." + path, "." + path1)

    def chmod(self, path, mode):
        os.chmod("." + path, mode)

    def chown(self, path, user, group):
        os.chown("." + path, user, group)

    def truncate(self, path, len):
        f = open("." + path, "a")
        f.truncate(len)
        f.close()

    def mknod(self, path, mode, dev):
        os.mknod("." + path, mode, dev)

    def mkdir(self, path, mode):
        os.mkdir("." + path, mode)

    def utime(self, path, times):
        os.utime("." + path, times)

    def access(self, path, mode):
        if not os.access("." + path, mode):
            return -EACCES

    def statfs(self):
        return os.statvfs(".")

    def fsinit(self):   
		os.chdir(self.root)
		self.gtask = GTaskThread(self.client)
		self.gtask.start()

    def fsdestroy(self):
		self.gtask.shutdown()


    class GoofsFile(object):

        def __init__(self, path, flags, *mode):
            self.file = os.fdopen(os.open("." + path, flags, *mode),
                                  flag2mode(flags))
            self.fd = self.file.fileno()

        def read(self, length, offset):
            self.file.seek(offset)
            return self.file.read(length)

        def write(self, buf, offset):
            self.file.seek(offset)
            self.file.write(buf)
            return len(buf)

        def release(self, flags):
            self.file.close()

        def _fflush(self):
            if 'w' in self.file.mode or 'a' in self.file.mode:
                self.file.flush()

        def fsync(self, isfsyncfile):
            self._fflush()
            if isfsyncfile and hasattr(os, 'fdatasync'):
                os.fdatasync(self.fd)
            else:
                os.fsync(self.fd)

        def flush(self):
            self._fflush()
            os.close(os.dup(self.fd))

        def fgetattr(self):
            return os.fstat(self.fd)

        def ftruncate(self, len):
            self.file.truncate(len)

        def lock(self, cmd, owner, **kw):
            op = { fcntl.F_UNLCK : fcntl.LOCK_UN,
                   fcntl.F_RDLCK : fcntl.LOCK_SH,
                   fcntl.F_WRLCK : fcntl.LOCK_EX }[kw['l_type']]
            if cmd == fcntl.F_GETLK:
                return -EOPNOTSUPP
            elif cmd == fcntl.F_SETLK:
                if op != fcntl.LOCK_UN:
                    op |= fcntl.LOCK_NB
            elif cmd == fcntl.F_SETLKW:
                pass
            else:
                return -EINVAL

            fcntl.lockf(self.fd, op, kw['l_start'], kw['l_len'])


    def main(self, *a, **kw):
        self.file_class = self.GoofsFile
        return Fuse.main(self, *a, **kw)

def main():	
    
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['user=', 'pw='])
    except getopt.error, msg:
        print 'python goofs.py --user [username] --pw [password] mntpoint '
        sys.exit(2)

    user = ''
    pw = ''
  	# Process options
    for option, arg in opts:
        if option == '--user':
            user = arg
        elif option == '--pw':
            pw = arg

    while not user:
        user = raw_input('Please enter your username: ')

    while not pw:
          pw = getpass.getpass()
          if not pw:
              print 'Password cannot be blank.'
                      
    init(user)


    usage = "Google filesystem" + Fuse.fusage

    server = Goofs(user, pw, version="%prog " + fuse.__version__,
                 usage=usage,
                 dash_s_do='setsingle')

    server.parser.add_option(mountopt="root", metavar="PATH", default=GOOFS_CACHE,
                             help="mirror filesystem from under PATH [default: %default]")
    server.parse(values=server, errex=1)

    try:
        if server.fuse_args.mount_expected():
            os.chdir(server.root)
    except OSError:
        print >> sys.stderr, "can't enter root of underlying filesystem"
        sys.exit(1)

    server.main()
	
if __name__ == '__main__':
    main()
