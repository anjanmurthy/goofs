from __future__ import with_statement
import threading
import thread
import atom
import os
from backend import *
import gdata.photos.service

def write(path, content):
    with open(path, 'w') as f:
        f.write(content)
            
def read(path):
    with open(path, 'r') as f:
        return f.read()

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

def remove_metadata(path):
    for ext in ['.self', '.edit']:
        if os.path.exists(path + ext):
            os.remove(path + ext)
            
class Event():
    def __init__(self, name):
        self.name = name

class UnlinkEvent(Event):
    def __init__(self, path):
        Event.__init__(self, 'unlink')
        self.path = path
        
class RmdirEvent(Event):
    def __init__(self, path):
        Event.__init__(self, 'rmdir')
        self.path = path
        
class RenameEvent(Event):
    def __init__(self, src_path, dest_path):
        Event.__init__(self, 'rename')
        self.src_path = src_path
        self.dest_path = dest_path

class MkdirEvent(Event):
    def __init__(self, path):
        Event.__init__(self, 'mkdir')
        self.path = path

class ReleaseEvent(Event):
    def __init__(self, path):
        Event.__init__(self, 'release')
        self.path = path
        
class EventHandler():
    def __init__(self, client):
        self.client = client

    def consumes(self, event):
        return False
    
    def consume(self, event):
        pass

class UnlinkEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
    
    def consumes(self, event):
        return event.name == 'unlink'

    def consume(self, event):
        uri = read(event.path + '.edit')
        self.client.delete_album_or_photo_by_uri(uri)
        remove_metadata(event.path)

class RmdirEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'rmdir'

    def consume(self, event):
        uri = read(event.path + '.edit')
        album = self.client.get_album_or_photo_by_uri(uri)
        self.client.delete_album(album)
        remove_metadata(event.path)

class RenameEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'rename'
        
    def consume(self, event):
        album_self = os.path.dirname(event.dest_path) + '.self'
        album = self.client.get_album_or_photo_by_uri(read(album_self))
        photo = self.client.upload_photo_with_path(album, event.src_path, event.dest_path)
        write(event.dest_path + '.self', photo.GetSelfLink().href)
        if photo.GetEditLink() is not None:
            write(event.dest_path + '.edit', photo.GetEditLink().href)
        ev = UnlinkEventHandler(self.client)
        ev.consume(UnlinkEvent(event.src_path))

class MkdirEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'mkdir'
        
    def consume(self, event):
        album = None
        if os.path.dirname(event.path).endswith('/photos/public'):
            album = self.client.upload_album(os.path.basename(event.path), 'public')
        else:
            album = self.client.upload_album(os.path.basename(event.path), 'private')
        write(event.path + '.self', album.GetSelfLink().href)
        if album.GetEditLink() is not None:
            write(event.path + '.edit', album.GetEditLink().href)
            
class ReleaseEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)

    def consumes(self, event):
        return event.name == 'release'
        
    def consume(self, event):
        if os.path.exists(event.path + '.self'):
            # existing photo
            existing_photo = self.client.get_album_or_photo_by_uri(read(event.path + '.self'))
            photo = self.client.upload_photo_blob(existing_photo, event.path)
        else:
            # new photo
            album_self = os.path.dirname(event.path) + '.self'
            album = self.client.get_album_or_photo_by_uri(read(album_self))
            photo = self.client.upload_photo(album, event.path)
        write(event.path + '.self', photo.GetSelfLink().href)
        if photo.GetEditLink() is not None:
            write(event.path + '.edit', photo.GetEditLink().href)

class TaskThread(threading.Thread):
    """Thread that executes a task every N seconds"""
    def __init__(self):
        threading.Thread.__init__(self)
        self._finished = threading.Event()
        self._interval = 30.0
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

class CleanupThread(TaskThread):
    def __init__(self, client, photo_dirs):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 60.0
        self.photo_dirs = photo_dirs

    def task(self):
        for dir in self.photo_dirs:
            for entry in os.listdir(dir):
                if os.path.isdir(os.path.join(dir, entry)):
                    for f in os.listdir(os.path.join(dir, entry)):
                        if os.path.isfile(os.path.join(dir, entry, f + '.self')):
                            try:
                                uri = read(os.path.join(dir, entry, f + '.self'))
                                photo = self.client.get_album_or_photo_by_uri(uri)
                            except gdata.photos.service.GooglePhotosException, ex:
                                remove_file_and_metadata(os.path.join(dir, entry, f))
                                
                    if os.path.isfile(os.path.join(dir, entry + '.self')):
                        try:
                            uri = read(os.path.join(dir, entry + '.self'))
                            album = self.client.get_album_or_photo_by_uri(uri)
                        except gdata.photos.service.GooglePhotosException, ex:
                            for root, dirs, files in os.walk(os.path.join(dir, entry), topdown=False):
                                for file in files:
                                    os.remove(os.path.join(root, file))
                            remove_dir_and_metadata(os.path.join(dir, entry))
                      
class DownloadThread(TaskThread):

    def __init__(self, client, photo_dirs):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 30.0
        self.photo_dirs = photo_dirs
                            
    def task(self):
        album_feed = self.client.albums_feed()
        for album in album_feed:
            if album.access.text == 'public':
                dir = self.photo_dirs[0]
            else:
                dir = self.photo_dirs[1]
            album_dir = os.path.join(dir, album.title.text)
            if not os.path.exists(album_dir):
                os.mkdir(album_dir)
                write(os.path.join(dir, album.title.text + '.self'), album.GetSelfLink().href)
                if album.GetEditLink() is not None:
                    write(os.path.join(dir, album.title.text + '.edit'), album.GetEditLink().href)
            photos_feed = self.client.photos_feed(album)
            for photo in photos_feed:    
                updated = self.client.get_photo_updated(photo)
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
       
