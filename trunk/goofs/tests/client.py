import gdata.photos.service
import unittest
from goofs.goofs import GClient
from goofs.goofs import write
from goofs.goofs import read
from goofs.goofs import remove_dir_and_metadata
from goofs.goofs import init
import goofs.goofs
import getpass
import os
import datetime

HOME = None
GOOFS_CACHE = None
PHOTOS = None
PHOTOS_DIR = None
PUB_PHOTOS_DIR = None
PRIV_PHOTOS_DIR = None
GDOCS_DIRS = None
EXT_CTYPE = {'bmp': 'image/bmp', 'gif': 'image/gif', 'png': 'image/png', 'jpg':'image/jpeg', 'jpeg':'image/jpeg'}


class ClientTestCase(unittest.TestCase):
            
    def setUp(self):
        
        user = None
        pw = None
        
        while not user:
            user = raw_input('Please enter your username: ')
        while not pw:
            pw = getpass.getpass()
            if not pw:
                print 'Password cannot be blank.'
        
        global HOME, GOOFS_CACHE, PHOTOS, PHOTOS_DIR, GDOCS_DIRS, PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR

        HOME = os.path.expanduser("~")
        GOOFS_CACHE = HOME + '/.goofs-cache/' + user.split('@')[0]
        PHOTOS = 'photos'
        PHOTOS_DIR = GOOFS_CACHE + '/' + PHOTOS
        PUB_PHOTOS_DIR = PHOTOS_DIR + '/public'
        PRIV_PHOTOS_DIR = PHOTOS_DIR + '/private'
        GDOCS_DIRS = [PUB_PHOTOS_DIR, PRIV_PHOTOS_DIR]
        
        init(user)

        self.client = GClient(user, pw)
        self.root = GOOFS_CACHE
        
        
    def tearDown(self):
        self.client = None




class WriteTestCase(ClientTestCase):
    
    def do_downloads(self):
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
   
    def write(self, path, buff, offset):
        print 'in write'
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
                        print 'writing contents'
                        write(name, buff)
                        print 'wrote contents'
                        album_uri = read(self.root + dir + '.self')
                        print 'got album uri - %s' % (album_uri)
                        album = self.client.get_album_or_photo_by_uri(album_uri)
                        print 'got album'
                        
                        photo = self.client.upload_photo(album_uri, name)
                        
                        print 'photo uploaded'
                        try:
                            assert(isinstance(photo, gdata.photos.PhotoEntry))
                            print 'got back a photo entry'
                        except AssertionError, ae:
                            print 'assertion failed'
                        write(name + '.self', photo.GetSelfLink().href)
                        print 'wrote self link'
                        if photo.GetEditLink() is not None:
                            write(name + '.edit', photo.GetEditLink().href)
                            print 'wrote edit link' 
                        return len(buff)
                except Exception, reason:
                    print 'handling exception'
                    print reason
        return -errno.EACCES

    

    
    def runTest(self):
        self.do_downloads()
        content = read('/home/rwynn/Pictures/robot.jpeg')
        assert content is not None
        self.write('/photos/public/Test/robot.jpeg', content, 0)
        


if __name__ == "__main__":
    unittest.main()

