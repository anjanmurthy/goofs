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
PUB_PHOTOS_DIR = None
PRIV_PHOTOS_DIR = None
QUERY_DIR = None
GDOCS_DIRS = None
EXT_CTYPE = {'bmp': 'image/bmp', 'gif': 'image/gif', 'png': 'image/png', 'jpg':'image/jpeg', 'jpeg':'image/jpeg'}

def remove_dir_and_metadata(path):
    for root, dirs, files in os.walk(path, topdown=False):
        for file in files:
            os.remove(os.path.join(root, file))
        for d in dirs:
            os.rmdir(os.path.join(root, d))
    for ext in ['.self', '.edit']:
        if os.path.exists(path + ext):
            os.remove(path + ext)
    os.rmdir(path)

def remove_file_and_metadata(path):
    os.remove(path)
    for ext in ['.self', '.edit']:
        if os.path.exists(path + ext):
            os.remove(path + ext)

def write(path, content):
    out = None
    try:
        out = open(path, 'w')
        out.write(content)
    finally:
        if out is not None:
            out.close()
            
def read(path):
    content = None
    inf = None
    try:
        inf = open(path, 'r')
        content = inf.read()
    finally:
        if inf is not None:
            inf.close()
    return content

def content_type_from_path(path):
    parts = path.split(os.extsep)
    if len(parts) > 0:
        return EXT_CTYPE[parts[-1]]
    else:
        return None

def flag2mode(flags):
    md = {os.O_RDONLY: 'r', os.O_WRONLY: 'w', os.O_RDWR: 'w+'}
    m = md[flags & (os.O_RDONLY | os.O_WRONLY | os.O_RDWR)]

    if flags | os.O_APPEND:
        m = m.replace('w', 'a', 1)

    return m

def init(user):
	
    global HOME, GOOFS_CACHE, PHOTOS, PHOTOS_DIR, GDOCS_DIRS, PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR, QUERY_DIR

    HOME = os.path.expanduser("~")
    GOOFS_CACHE = os.path.join(HOME, '.goofs-cache', user.split('@')[0])
    PHOTOS = 'photos'
    PHOTOS_DIR = os.path.join(GOOFS_CACHE, PHOTOS)
    PUB_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'public')
    PRIV_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'private')
    QUERY_DIR = os.path.join(PHOTOS_DIR, 'queries')
    GDOCS_DIRS = [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR, QUERY_DIR]
    
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
        self._interval = 60.0
        self.setDaemon ( True )
    
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

    def get_album_or_photo_by_uri(self, uri):
        return self.ph_client.GetEntry(uri)
    
    def photos_feed(self, album):
        return self.ph_client.GetFeed(album.GetPhotosUri()).entry

    def upload_photo(self, album, path):    
        return self.ph_client.InsertPhotoSimple(album, os.path.basename(path), os.path.basename(path), path, content_type_from_path(path))

    def update_photo(self, photo, path):
        return self.ph_client.UpdatePhotoBlob(photo, path, content_type_from_path(path))

    def upload_album(self, album_name, pub_or_priv):
        return self.ph_client.InsertAlbum(album_name, album_name, access=pub_or_priv)
    
    def get_photo(self, uri):
        return self.ph_client.GetEntry(uri)
    
    def delete_album_or_photo_by_uri(self, uri):
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
    
    def search_photos_feed(self, query):
        return self.ph_client.SearchUserPhotos(query).entry

	
class GTaskThread(TaskThread):

    def __init__(self, client):
        TaskThread.__init__(self)
        self.client = client
    
    def _do_searches(self):
        if os.path.exists(QUERY_DIR):
            for root, dirs, files in os.walk(GOOFS_CACHE, topdown=False):
                for d in dirs:
                    photos_feed = self.client.search_photos_feed(d)
                    for photo in photos_feed:
                         name = os.path.join(root, dir, photo.title.text)
                         write(name, self.client.get_photo_content(photo))
                         write(name + '.self', photo.GetSelfLink().href)
                         if photo.GetEditLink() is not None:
                             write(name + '.edit', photo.GetEditLink().href)
    def _do_downloads(self):
        album_feed = self.client.albums_feed()
        for album in album_feed:
            if album.access.text == 'public':
                dir = PUB_PHOTOS_DIR
            else:
                dir = PRIV_PHOTOS_DIR
            photos_feed = self.client.photos_feed(album)
            for photo in photos_feed:    
                updated = self.client.get_photo_updated(photo)
                album_dir = dir + '/' + album.title.text
                if not os.path.exists(album_dir):
                    os.mkdir(album_dir)
                    write(dir + '/' + album.title.text + '.self', album.GetSelfLink().href)
                    if album.GetEditLink() is not None:
                        write(dir + '/' + album.title.text + '.edit', album.GetEditLink().href)
                name = os.path.join(album_dir, photo.title.text)
                get = False                        
                if os.path.exists(name):
                    mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime = os.stat(name)
                    if updated > datetime.datetime.fromtimestamp(mtime):
                        get = True
                else:
                    get = True
                if get:
                    write(name, self.client.get_photo_content(photo))
                    write(name + '.self', photo.GetSelfLink().href)
                    if photo.GetEditLink() is not None:
                        write(name + '.edit', photo.GetEditLink().href)        
                                
    def task(self):
        self._do_downloads()
        
        #self._do_searches()
        




class Goofs(Fuse):

    def __init__(self, user, pw, *args, **kw):
        Fuse.__init__(self, *args, **kw)
        self.root = GOOFS_CACHE
        self.user = user
        self.pw = pw
        self.client = GClient(self.user, self.pw)
	
    def getattr(self, path):
        return os.lstat(self.root + path)
    
    def read(self, path, size, offset):
        f=open(self.root + path, 'r')
        f.seek(offset)
        content = f.read(size)
        f.close()
        return content
    
    def write(self, path, buff, offset):
        
        file_ext = os.path.basename(path).split(os.extsep)
        if len(file_ext) < 2 or not file_ext[1] in ['bmp', 'gif', 'png', 'jpeg', 'jpg']:
            return -errno.EACCES
        name = self.root + path
        dir = os.path.dirname(path)
        if dir.startswith('/photos/public') or dir.startwith('/photos/private'):
            photo_dir, album = os.path.split(dir)
            if photo_dir in ['/photos/public', '/photos/private']:
                try:
                    if os.path.exists(self.root + dir + '.self'):
                        
                        write(name, buff)
                        
                        album_uri = read(self.root + dir + '.self')
                        
                        album = self.client.get_album_or_photo_by_uri(album_uri)
                        
                        photo = self.client.upload_photo(album_uri, name)
                        
                        write(name + '.self', photo.GetSelfLink().href)
                        
                        if photo.GetEditLink() is not None:
                            write(name + '.edit', photo.GetEditLink().href)
 
                        return len(buff)
                except Exception, reason:
                    if os.path.exists(name):
                        os.unlink(name)
                        return 0
        return -errno.EACCES
    
    def readlink(self, path):
        return os.readlink(self.root + path)

    def readdir(self, path, offset):
        for e in os.listdir(self.root + path):
            if not e.endswith('.self') and not e.endswith('.edit'):
                yield fuse.Direntry(e)
                
    def unlink(self, path):
        if os.path.exists(self.root + path + '.edit'):
            uri = read(self.root + path + '.edit')
            self.client.delete_album_or_photo_by_uri(uri)
            remove_file_and_metadata(self.root + path)
        else:
           return -errno.EACCES

    def rmdir(self, path):
        if os.path.exists(self.root + path + '.edit'):
            uri = read(self.root + path + '.edit')
            self.client.delete_album_or_photo_by_uri(uri)
            remove_dir_and_metadata(self.root + path)
            os.chdir(self.root)
        else:
            return -errno.EACCES

    def symlink(self, path, path1):
        os.symlink(path, self.root + path1)

    def rename(self, path, path1):
        os.rename(self.root + path, self.root + path1)

    def link(self, path, path1):
        os.link(self.root + path, self.root + path1)

    def chmod(self, path, mode):
        os.chmod(self.root + path, mode)

    def chown(self, path, user, group):
        os.chown(self.root + path, user, group)

    def truncate(self, path, len):
        f = open(self.root + path, "a")
        f.truncate(len)
        f.close()

    def mknod(self, path, mode, dev):
        os.mknod(self.root + path, mode, dev)

    def mkdir(self, path, mode):
        if os.path.dirname(path) in ['/photos/public', '/photos/private']:
            album = None
            if os.path.dirname(path) == '/photos/public':
                album = self.client.upload_album(os.path.basename(path), 'public')
                dir = PUB_PHOTOS_DIR
            else:
                album = self.client.upload_album(os.path.basename(path), 'private')
                dir = PRIV_PHOTOS_DIR
            
            if album:
                album_dir = dir + '/' + album.title.text
                os.mkdir(album_dir, mode)
                write(dir + '/' + album.title.text + '.self', album.GetSelfLink().href)
                if album.GetEditLink() is not None:
                    write(dir + '/' + album.title.text + '.edit', album.GetEditLink().href)    
        else:
            return -errno.EACCES
            
    def utime(self, path, times):
        os.utime(self.root + path, times)

    def access(self, path, mode):
        if not os.access(self.root + path, mode):
            return -EACCES

    def statfs(self):
        return os.statvfs(self.root)

    def fsinit(self):   
		os.chdir(self.root)
		self.gtask = GTaskThread(self.client)
		self.gtask.start()

    def fsdestroy(self):
		self.gtask.shutdown()

    def main(self, *a, **kw):
        return Fuse.main(self, *a, **kw)

def main():	
    
    """
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
    """
    user = 'bigwynnr@gmail.com'
    pw = 'jane804lane'

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
