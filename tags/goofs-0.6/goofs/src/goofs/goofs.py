#!/usr/bin/env python


import os
import getpass
import getopt
import sys
import errno
from errno import *
from stat import *
import fcntl
from backend import *
from background import *
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
CONTACTS_DIR = None
BLOGS_DIR = None
DOCS_DIR = None
CAL_DIR = None
SPREADS_DIR = None
GDOCS_DIRS = None
CLIENT = None


def flag2mode(flags):
    md = {os.O_RDONLY: 'r', os.O_WRONLY: 'w', os.O_RDWR: 'w+'}
    m = md[flags & (os.O_RDONLY | os.O_WRONLY | os.O_RDWR)]

    if flags | os.O_APPEND:
        m = m.replace('w', 'a', 1)

    return m

def init(user, pw):
    
    global CLIENT, HOME, GOOFS_CACHE, DOCS_DIR, SPREADS_DIR, PRESENTS_DIR, BLOGS_DIR, CAL_DIR, CONTACTS_DIR, PHOTOS, PHOTOS_DIR, GDOCS_DIRS, PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR

    CLIENT = GClient(user, pw)

    HOME = os.path.expanduser("~")
    GOOFS_CACHE = os.path.join(HOME, '.goofs-cache', user.split('@')[0])
    PHOTOS = 'photos'
    PHOTOS_DIR = os.path.join(GOOFS_CACHE, PHOTOS)
    CONTACTS_DIR = os.path.join(GOOFS_CACHE, 'contacts')
    BLOGS_DIR = os.path.join(GOOFS_CACHE, 'blogs')
    CAL_DIR = os.path.join(GOOFS_CACHE, 'calendars')
    DOCS_DIR = os.path.join(GOOFS_CACHE, 'documents')
    SPREADS_DIR = os.path.join(GOOFS_CACHE, 'spreadsheets')
    PRESENTS_DIR = os.path.join(GOOFS_CACHE, 'presentations')
    PUB_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'public')
    PRIV_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'private')
    GDOCS_DIRS = [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR, CONTACTS_DIR, BLOGS_DIR, CAL_DIR, DOCS_DIR, SPREADS_DIR, PRESENTS_DIR]
    
    for root, dirs, files in os.walk(GOOFS_CACHE, topdown=False):
        for file in files:
            os.remove(os.path.join(root, file))
        for d in dirs:
            os.rmdir(os.path.join(root, d))
            
    for d in GDOCS_DIRS:
        os.makedirs(d)
    

class Goofs(Fuse):

    def __init__(self, *args, **kw):
        Fuse.__init__(self, *args, **kw)
        self.user = None
        self.pw = None
        
    def login(self):
        while not self.user:
            self.user = raw_input('Please enter your username: ')
            if not self.user:
                print 'Username cannot be blank.'
        while not self.pw:
            self.pw = getpass.getpass()
            if not self.pw:
                print 'Password cannot be blank.'
        init(self.user, self.pw)
        self.root = GOOFS_CACHE

    def getattr(self, path):
        return os.lstat(self.root + path)
    
    def readlink(self, path):
        return os.readlink(self.root + path)
                
    def getattr(self, path):
        return os.lstat("." + path)

    def readlink(self, path):
        return os.readlink("." + path)

    def readdir(self, path, offset):
        for e in os.listdir("." + path):
            if not e.endswith('.self') and not e.endswith('.edit'):
                yield fuse.Direntry(e)

    def unlink(self, path):
        ev = UnlinkEventHandler(CLIENT)
        ev.consume(UnlinkEvent(GOOFS_CACHE + path))
        os.unlink("." + path)

    def rmdir(self, path):
        if os.path.dirname(path) in ['/', '/calendars']:
            return -errno.EACCES
        else:
            ev = RmdirEventHandler(CLIENT)
            ev.consume(RmdirEvent(GOOFS_CACHE + path))
            os.rmdir("." + path)

    def symlink(self, path, path1):
        os.symlink(path, "." + path1)

    def rename(self, path, path1):
        if os.path.dirname(path) in ['/', '/documents', '/spreadsheets', '/presentations'] and os.path.isdir("." + path):
            return -errno.EACCES
        else:
            os.rename("." + path, "." + path1)
            ev = RenameEventHandler(CLIENT)
            ev.consume(RenameEvent(GOOFS_CACHE + path, GOOFS_CACHE + path1))
            
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
        if os.path.dirname(path) in ['/photos/public', '/photos/private', '/contacts'] or os.path.dirname(os.path.dirname(path)) in ['/blogs', '/calendars']:
            ev = MkdirEventHandler(CLIENT)
            ev.consume(MkdirEvent(GOOFS_CACHE + path))
            if not os.path.isdir("." + path):
                os.mkdir("." + path, mode)
        else:
            return -errno.EACCES

    def utime(self, path, times):
        os.utime("." + path, times)

    def access(self, path, mode):
        if not os.access("." + path, mode):
            return -EACCES
        
    def statfs(self):
        return os.statvfs(".")

    def fsinit(self):   
        os.chdir(self.root)
        self.threads = [CalendarDownloadThread(CLIENT, CAL_DIR), DocsDownloadThread(CLIENT, DOCS_DIR, SPREADS_DIR, PRESENTS_DIR), PhotosDownloadThread(CLIENT, [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR]), ContactsDownloadThread(CLIENT, CONTACTS_DIR), BlogsDownloadThread(CLIENT, BLOGS_DIR), PhotosCleanupThread(CLIENT, [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR]), ContactsCleanupThread(CLIENT, CONTACTS_DIR), BlogsCleanupThread(CLIENT, BLOGS_DIR), DocsCleanupThread(CLIENT, DOCS_DIR), CalendarCleanupThread(CLIENT, CAL_DIR)]
        for thread in self.threads:
            thread.start()
            
    def fsdestroy(self):
        for thread in self.threads:
            thread.shutdown();
        
    class GoofsFile(object):

        def __init__(self, path, flags, *mode):
            self.file = os.fdopen(os.open(GOOFS_CACHE + path, flags, *mode),
                                  flag2mode(flags))
            self.fd = self.file.fileno()
            self.path = path
            self.written_to = False

        def read(self, length, offset):
            self.file.seek(offset)
            return self.file.read(length)

        def write(self, buf, offset):
            self.written_to = True
            self.file.seek(offset)
            self.file.write(buf)
            return len(buf)

        def release(self, flags):
            self.file.close()
            if self.written_to:
                self.written_to = False
                ev = ReleaseEventHandler(CLIENT)
                ev.consume(ReleaseEvent(GOOFS_CACHE + self.path))

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
            # cf. xmp_flush() in fusexmp_fh.c
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

    usage = "Google filesystem" + Fuse.fusage

    server = Goofs(version="%prog " + fuse.__version__,
                 usage=usage,
                 dash_s_do='setsingle')

    server.parser.add_option(mountopt="root", metavar="PATH", default=GOOFS_CACHE,
                             help="mirror filesystem from under PATH [default: %default]")
    
    server.parser.add_option('--user', action="store", type="string", dest="user", help='username')

    server.parser.add_option('--pw', action="store", type="string", dest="pw", help='password')
    
    server.parse(values=server, errex=1)
    
    server.login()

    try:
        if server.fuse_args.mount_expected():
            os.chdir(server.root)
    except OSError:
        print >> sys.stderr, "can't enter root of underlying filesystem"
        sys.exit(1)

    server.main()
    
if __name__ == '__main__':
    main()