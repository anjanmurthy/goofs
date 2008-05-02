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
GDOCS_DIRS = None
CLIENT = None


def flag2mode(flags):
    md = {os.O_RDONLY: 'r', os.O_WRONLY: 'w', os.O_RDWR: 'w+'}
    m = md[flags & (os.O_RDONLY | os.O_WRONLY | os.O_RDWR)]

    if flags | os.O_APPEND:
        m = m.replace('w', 'a', 1)

    return m

def init(user, pw):
    
    global CLIENT, HOME, GOOFS_CACHE, CONTACTS_DIR, PHOTOS, PHOTOS_DIR, GDOCS_DIRS, PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR

    CLIENT = GClient(user, pw)

    HOME = os.path.expanduser("~")
    GOOFS_CACHE = os.path.join(HOME, '.goofs-cache', user.split('@')[0])
    PHOTOS = 'photos'
    PHOTOS_DIR = os.path.join(GOOFS_CACHE, PHOTOS)
    CONTACTS_DIR = os.path.join(GOOFS_CACHE, 'contacts')
    PUB_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'public')
    PRIV_PHOTOS_DIR = os.path.join(PHOTOS_DIR, 'private')
    GDOCS_DIRS = [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR, CONTACTS_DIR]
    
    for root, dirs, files in os.walk(GOOFS_CACHE, topdown=False):
        for file in files:
            os.remove(os.path.join(root, file))
        for d in dirs:
            os.rmdir(os.path.join(root, d))
            
    for d in GDOCS_DIRS:
        os.makedirs(d)
    

class Goofs(Fuse):

    def __init__(self, user, pw, *args, **kw):
        Fuse.__init__(self, *args, **kw)
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
        ev = RmdirEventHandler(CLIENT)
        ev.consume(RmdirEvent(GOOFS_CACHE + path))
        os.rmdir("." + path)

    def symlink(self, path, path1):
        os.symlink(path, "." + path1)

    def rename(self, path, path1):
        ev = RenameEventHandler(CLIENT)
        ev.consume(RenameEvent(GOOFS_CACHE + path, GOOFS_CACHE + path1))
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
        if os.path.dirname(path) in ['/photos/public', '/photos/private', '/contacts']:
            ev = MkdirEventHandler(CLIENT)
            ev.consume(MkdirEvent(GOOFS_CACHE + path))
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
        self.dtask = DownloadThread(CLIENT, [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR], CONTACTS_DIR)
        self.dtask.start()
        self.ctask = CleanupThread(CLIENT, [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR], CONTACTS_DIR)
        self.ctask.start()
        
    def fsdestroy(self):
        self.dtask.shutdown()
        self.ctask.shutdown()
        
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

    user = ''
    pw = ''
    
    """
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['user=', 'pw='])
    except getopt.error, msg:
        print 'python goofs.py --user [username] --pw [password] mntpoint '
        sys.exit(2)
        
    # Process options
    for option, arg in opts:
        if option == '--user':
            user = arg
        elif option == '--pw':
            pw = arg
    """

    while not user:
        user = raw_input('Please enter your username: ')

    while not pw:
          pw = getpass.getpass()
          if not pw:
              print 'Password cannot be blank.'
                      
    init(user, pw)

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
