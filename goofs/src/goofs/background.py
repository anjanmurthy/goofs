from __future__ import with_statement
import threading
import thread
import atom
import os
from backend import *
import gdata.photos.service
import gdata.contacts.service
from gdata.contacts import ContactEntry

def service_from_path(path, username):
    parts = path.split('/')
    for i, part in enumerate(parts):
        if part == username:
            return parts[i+1]
    return None

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
        service = service_from_path(event.path, self.client.get_username())
        if service == 'photos':
            uri = read(event.path + '.edit')
            self.client.delete_album_or_photo_by_uri(uri)
            remove_metadata(event.path)
        elif service == 'blogs':
            if os.path.exists(event.path + '.edit'):
                self.client.delete_blog_entity(read(event.path + '.edit'))
                remove_metadata(event.path)

class RmdirEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'rmdir'

    def consume(self, event):
        service = service_from_path(event.path, self.client.get_username())
        if service == 'photos':
            uri = read(event.path + '.edit')
            album = self.client.get_album_or_photo_by_uri(uri)
            self.client.delete_album(album)
            remove_metadata(event.path)
        elif service == 'contacts':
            if os.path.dirname(event.path).endswith('/contacts'): 
                contact = self.client.get_contact_by_uri(read(event.path + '.self'))
                self.client.delete_contact(contact.GetEditLink().href)
                remove_metadata(event.path)
        elif service == 'blogs':
            if os.path.exists(event.path + '.edit'):
                self.client.delete_blog_entity(read(event.path + '.edit'))
                remove_metadata(event.path)

class RenameEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'rename'
        
    def consume(self, event):
        service = service_from_path(event.dest_path, self.client.get_username())
        if service == 'photos':
            album_self = os.path.dirname(event.src_path) + '.self'
            album = self.client.get_album_or_photo_by_uri(read(album_self))
            photo = self.client.upload_photo_with_path(album, event.src_path, event.dest_path)
            write(event.dest_path + '.self', photo.GetSelfLink().href)
            if photo.GetEditLink() is not None:
                write(event.dest_path + '.edit', photo.GetEditLink().href)
            ev = UnlinkEventHandler(self.client)
            ev.consume(UnlinkEvent(event.src_path))
        elif service == 'contacts':
            if os.path.dirname(event.src_path).endswith('/contacts') and os.path.dirname(event.dest_path).endswith('/contacts'):
                contact_self = event.src_path + '.self'
                contact = self.client.get_contact_by_uri(read(contact_self))
                contact.title.text = os.path.basename(event.dest_path)
                self.client.update_contact(contact.GetEditLink().href, contact)
                write(event.dest_path + '.self', contact.GetSelfLink().href)
                if contact.GetEditLink() is not None:
                    write(event.dest_path + '.edit', contact.GetEditLink().href)
                remove_metadata(event.src_path)
        elif service == 'blogs':
            if os.path.dirname(os.path.dirname(event.src_path)).endswith('/blogs') and os.path.dirname(os.path.dirname(event.dest_path)).endswith('/blogs'):
                post = self.client.get_blog_post(read(event.src_path + '.self'))
                post.title = atom.Title('xhtml', os.path.basename(event.dest_path))
                new_post = self.client.update_blog_post(post)
                write(event.dest_path + '.self', new_post.GetSelfLink().href)
                if new_post.GetEditLink() is not None:
                    write(event.dest_path + '.edit', new_post.GetEditLink().href)
                remove_metadata(event.src_path)
                

class MkdirEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)
        
    def consumes(self, event):
        return event.name == 'mkdir'
        
    def consume(self, event):
        service = service_from_path(event.path, self.client.get_username())
        if service == 'photos':
            album = None
            if os.path.dirname(event.path).endswith('/photos/public'):
                album = self.client.upload_album(os.path.basename(event.path), 'public')
            else:
                album = self.client.upload_album(os.path.basename(event.path), 'private')
            write(event.path + '.self', album.GetSelfLink().href)
            if album.GetEditLink() is not None:
                write(event.path + '.edit', album.GetEditLink().href)
        elif service == 'contacts':
            if os.path.dirname(event.path).endswith('/contacts'):
                new_contact = gdata.contacts.ContactEntry()
                new_contact.title = atom.Title(text=os.path.basename(event.path))
                contact = self.client.upload_contact(new_contact)
                write(event.path + '.self', contact.GetSelfLink().href)
                if contact.GetEditLink() is not None:
                    write(event.path + '.edit', contact.GetEditLink().href)
        elif service == 'blogs':
            if os.path.dirname(os.path.dirname(event.path)).endswith('/blogs'):
                blog_id = self.client.get_blog_id_from_uri(read(os.path.dirname(event.path) + '.self'))
                post = gdata.GDataEntry()
                post.author.append(atom.Author(atom.Name(text=self.client.get_username())))
                post.title = atom.Title(title_type='xhtml', text=os.path.basename(event.path))
                post.content = atom.Content(content_type='html', text='')
                new_post = self.client.create_blog_post(blog_id, post)
                write(event.path + '.self', new_post.GetSelfLink().href)
                if new_post.GetEditLink() is not None:
                    write(event.path + '.edit', new_post.GetEditLink().href)
       
class ReleaseEventHandler(EventHandler):
    def __init__(self, client):
        EventHandler.__init__(self, client)

    def consumes(self, event):
        return event.name == 'release'
        
    def consume(self, event):
        service = service_from_path(event.path, self.client.get_username())
        if service == 'photos':       
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
        elif service == 'contacts':
            if os.path.basename(event.path) not in ['work', 'home', 'other' , 'notes', 'organization']:
                return
            if os.path.exists(os.path.dirname(event.path) + '.self'):
                self_uri = read(os.path.dirname(event.path) + '.self')
                edit_uri = read(os.path.dirname(event.path) + '.edit')
                contact_field = os.path.basename(event.path)
            else:
                self_uri = read(os.path.dirname(os.path.dirname(event.path)) + '.self')
                edit_uri = read(os.path.dirname(os.path.dirname(event.path)) + '.edit')
                contact_field = os.path.basename(os.path.dirname(event.path))
            contact = self.client.get_contact_by_uri(self_uri)
            field_val = read(event.path)
            if contact_field == 'email':
                data = gdata.contacts.Email(rel='http://schemas.google.com/g/2005#%s' % os.path.basename(event.path),primary='true',address=field_val)
                if len(contact.email) == 0:
                    contact.email.append(data)
                else:
                    updated = False
                    for i, e in enumerate(contact.email):
                        if e.rel.endswith(os.path.basename(event.path)):
                            contact.email[i] = data
                            updated = True
                            break
                    if not updated:
                        contact.email.append(data)
            elif contact_field == 'phone':
                data = gdata.contacts.PhoneNumber(rel='http://schemas.google.com/g/2005#%s' % os.path.basename(event.path), text=field_val)
                if len(contact.phone_number) == 0:
                    contact.phone_number.append(data)
                else:
                    updated = False
                    for i, p in enumerate(contact.phone_number):
                        if p.rel.endswith(os.path.basename(event.path)):
                            contact.phone_number[i] = data
                            updated = True
                            break
                    if not updated:
                        contact.phone_number.append(data)
            elif contact_field == 'address':
                data = gdata.contacts.PhoneNumber(rel='http://schemas.google.com/g/2005#%s' % os.path.basename(event.path), text=field_val)
                if len(contact.postal_address) == 0:
                    contact.postal_address.append(data)
                else:
                    updated = False
                    for i, a in enumerate(contact.postal_address):
                        if a.rel.endswith(os.path.basename(event.path)):
                            contact.postal_address[i] = data
                            updated = True
                            break
                    if not updated:
                        contact.postal_address.append(data)    
            elif contact_field == 'notes':
                data = atom.Content(text=field_val)
                contact.content = data
            elif contact_field == 'organization':
                data = gdata.contacts.Organization(org_name=gdata.contacts.OrgName(text=field_val), rel='http://schemas.google.com/g/2005#work')
                contact.organization = data
            self.client.update_contact(edit_uri, contact)
        elif service == 'blogs':
            if os.path.basename(event.path) in ['content']:
                post = self.client.get_blog_post(read(os.path.dirname(event.path) + '.self'))
                post.content = atom.Content(content_type='html', text=read(event.path))
                new_post = self.client.update_blog_post(post)
                write(event.path + '.self', new_post.GetSelfLink().href)
                if new_post.GetEditLink() is not None:
                    write(event.path + '.edit', new_post.GetEditLink().href)
            elif os.path.dirname(event.path).endswith('/comments'):
                if os.path.exists(event.path + '.self'):
                    comment = self.client.get_blog_comment(read(event.path + '.self'))
                    comment.content = atom.Content(content_type='xhtml', text=read(event.path))
                    new_comment = self.client.update_blog_comment(comment)
                    remove_metadata(event.path)
                else:
                    comment = gdata.GDataEntry()
                    comment.content = atom.Content(content_type='xhtml', text=read(event.path))
                    blog_self = read(os.path.dirname(os.path.dirname(os.path.dirname(event.path))) + '.self')
                    post_self = read(os.path.dirname(os.path.dirname(event.path)) + '.self')
                    blog_id = self.client.get_blog_id_from_uri(blog_self)
                    post_id = self.client.get_post_id_from_uri(post_self)
                    new_comment = self.client.create_blog_post_comment(blog_id, post_id, comment)
                new_path = os.path.join(os.path.dirname(event.path), new_comment.title.text)
                os.rename(event.path, new_path)
                write(new_path + '.self', new_comment.GetSelfLink().href)
                if new_comment.GetEditLink() is not None:
                    write(new_path + '.edit', new_comment.GetEditLink().href)
                        
                
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


class PhotosCleanupThread(TaskThread):
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


class ContactsCleanupThread(TaskThread):
    def __init__(self, client, contact_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 60.0
        self.contact_base_dir = contact_base_dir

    def task(self):
        for contact_dir in os.listdir(self.contact_base_dir):
            if os.path.isfile(os.path.join(self.contact_base_dir, contact_dir + '.self')):
                try:
                    uri = read(os.path.join(self.contact_base_dir, contact_dir + '.self'))
                    contact = self.client.get_contact_by_uri(uri)
                except Exception, ex:
                    for root, dirs, files in os.walk(os.path.join(self.contact_base_dir, contact_dir), topdown=False):
                        for file in files:
                            os.remove(os.path.join(root, file))
                    remove_dir_and_metadata(os.path.join(self.contact_base_dir, contact_dir))
                    

class BlogsCleanupThread(TaskThread):
    def __init__(self, client, blog_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 60.0
        self.blog_base_dir = blog_base_dir

    def task(self):
        for blog_dir in os.listdir(self.blog_base_dir):
            if os.path.isfile(os.path.join(self.blog_base_dir, blog_dir + '.self')):
                try:
                    uri = read(os.path.join(self.blog_base_dir, blog_dir + '.self'))
                    blog = self.client.get_blog(uri)
                except Exception, ex:
                    for root, dirs, files in os.walk(os.path.join(self.blog_base_dir, blog_dir), topdown=False):
                        for file in files:
                            os.remove(os.path.join(root, file))
                    remove_dir_and_metadata(os.path.join(self.blog_base_dir, blog_dir))
                for post_dir in os.listdir(os.path.join(self.blog_base_dir, blog_dir)):
                    if os.path.isfile(os.path.join(self.blog_base_dir, blog_dir, post_dir + '.self')):
                        try:
                            uri = read(os.path.join(self.blog_base_dir, blog_dir, post_dir + '.self'))
                            post = self.client.get_blog_post(uri)
                        except Exception, ex:
                            for root, dirs, files in os.walk(os.path.join(self.blog_base_dir, blog_dir, post_dir), topdown=False):
                                for file in files:
                                    os.remove(os.path.join(root, file))
                            remove_dir_and_metadata(os.path.join(self.blog_base_dir, blog_dir, post_dir))
    

class PhotosDownloadThread(TaskThread):
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

class ContactsDownloadThread(TaskThread):
    def __init__(self, client, contact_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 30.0
        self.contact_base_dir = contact_base_dir
        
    def task(self):            
        contacts_feed = self.client.contacts_feed()
        for contact in contacts_feed:
            if contact.title.text is not None:
                updated = self.client.get_contact_updated(contact)
                contact_dir = os.path.join(self.contact_base_dir, contact.title.text)
                if os.path.exists(contact_dir):
                    mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime = os.stat(contact_dir)
                    if updated < datetime.datetime.fromtimestamp(mtime):
                        continue
                write(contact_dir + '.self', contact.GetSelfLink().href)
                if contact.GetEditLink() is not None:
                    write(contact_dir + '.edit', contact.GetEditLink().href)
                if not os.path.exists(contact_dir):
                    os.mkdir(contact_dir)
                if not os.path.exists(os.path.join(contact_dir, 'email')):
                    os.mkdir(os.path.join(contact_dir, 'email'))
                if not os.path.exists(os.path.join(contact_dir, 'phone')):
                    os.mkdir(os.path.join(contact_dir, 'phone'))
                if not os.path.exists(os.path.join(contact_dir, 'address')):
                    os.mkdir(os.path.join(contact_dir, 'address'))
                email_work = False
                email_home = False
                email_other = False
                if len(contact.email) > 0:    
                    for e in contact.email:
                        if e.rel.endswith('work'):
                            email_work = True
                            write(os.path.join(contact_dir, 'email', 'work'), e.address)
                        elif e.rel.endswith('home'):
                            email_home = True
                            write(os.path.join(contact_dir, 'email', 'home'), e.address)
                        else:
                            email_other = True
                            write(os.path.join(contact_dir, 'email', 'other'), e.address)
                if not email_work:
                    write(os.path.join(contact_dir, 'email', 'work'), '')
                if not email_home:
                    write(os.path.join(contact_dir, 'email', 'home'), '')
                if not email_other:
                    write(os.path.join(contact_dir, 'email', 'other'), '')
                phone_work = False
                phone_home = False
                phone_other = False
                if len(contact.phone_number) > 0:
                    for p in contact.phone_number:
                        if p.rel.endswith('work'):
                            phone_work = True
                            write(os.path.join(contact_dir, 'phone', 'work'), p.text)
                        elif p.rel.endswith('home'):
                            phone_home = True
                            write(os.path.join(contact_dir, 'phone', 'home'), p.text)
                        else:
                            phone_other = True
                            write(os.path.join(contact_dir, 'phone', 'other'), p.text)
                if not phone_work:
                    write(os.path.join(contact_dir, 'phone', 'work'), '')
                if not phone_home:
                    write(os.path.join(contact_dir, 'phone', 'home'), '')
                if not phone_other:
                    write(os.path.join(contact_dir, 'phone', 'other'), '')
                address_work = False
                address_home = False
                address_other = False
                if len(contact.postal_address) > 0:
                    for a in contact.postal_address:
                        if a.rel.endswith('work'):
                            address_work = True
                            write(os.path.join(contact_dir, 'address', 'work'), a.text)
                        elif a.rel.endswith('home'):
                            address_home = True
                            write(os.path.join(contact_dir, 'address', 'home'), a.text)
                        else:
                            address_other = True
                            write(os.path.join(contact_dir, 'address', 'other'), a.text)
                if not address_work:
                    write(os.path.join(contact_dir, 'address', 'work'), '')
                if not address_home:
                    write(os.path.join(contact_dir, 'address', 'home'), '')
                if not address_other:
                    write(os.path.join(contact_dir, 'address', 'other'), '')
                if contact.content is not None and contact.content.text is not None:
                    write(os.path.join(contact_dir, 'notes'), contact.content.text)
                else:
                    write(os.path.join(contact_dir, 'notes'), '')
                if contact.organization is not None and contact.organization.org_name is not None:
                    write(os.path.join(contact_dir, 'organization'), contact.organization.org_name.text)
                else:
                    write(os.path.join(contact_dir, 'organization'), '')
                                 
class BlogsDownloadThread(TaskThread):

    def __init__(self, client, blogs_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 30.0
        self.blogs_base_dir = blogs_base_dir
                            
    def task(self):
        blogs_feed = self.client.blogs_feed()
        for blog in blogs_feed:
            blog_dir = os.path.join(self.blogs_base_dir, blog.title.text)
            if not os.path.exists(blog_dir):
                os.mkdir(blog_dir)
                write(blog_dir + '.self', blog.GetSelfLink().href)
                if blog.GetEditLink() is not None:
                    write(blog_dir + '.edit', blog.GetEditLink().href)
            posts_feed = self.client.get_blog_posts(blog)
            for post in posts_feed:
                post_dir = os.path.join(self.blogs_base_dir, blog.title.text, post.title.text)
                if not os.path.exists(post_dir):
                    os.mkdir(post_dir)
                    write(post_dir + '.self', post.GetSelfLink().href)
                    if post.GetEditLink() is not None:
                        write(post_dir + '.edit', post.GetEditLink().href)
                if post.content.text is not None:
                    write(os.path.join(post_dir, 'content'), post.content.text)
                else:
                    write(os.path.join(post_dir, 'content'), '')
                comment_dir = os.path.join(post_dir, 'comments')
                if not os.path.exists(comment_dir):
                    os.mkdir(comment_dir)
                comments_feed = self.client.get_post_comments(blog, post)
                for comment in comments_feed:
                    write(os.path.join(comment_dir, comment.title.text), comment.content.text)
                    write(os.path.join(comment_dir, comment.title.text + '.self'), comment.GetSelfLink().href)
                    if comment.GetEditLink() is not None:
                         write(os.path.join(comment_dir, comment.title.text + '.edit'), comment.GetEditLink().href)      
