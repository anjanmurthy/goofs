from __future__ import with_statement
import threading
import thread
import atom
import os
import re
from backend import *
import gdata
import gdata.photos.service
import gdata.contacts.service
import gdata.docs.service
import gdata.calendar
from gdata.contacts import ContactEntry
from gdata.service import RequestError
import logging

REGEX_DF = '(19|20)\d\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])'
TODAY_STR = 'Today'
SEV_DAYS_STR = '7_Days'
THIRTY_DAYS_STR = '30_Days'


def is_date_range(file_name):
    return re.match(REGEX_DF + '-' + REGEX_DF, file_name) is not None

def get_file_ext(file_name):
    match = re.search('.*\.([a-zA-Z]{3,}$)', file_name)
    if match:
      return match.group(1).upper()
    return ''

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
            
            
def write_cal_entries(client, cal_dir, entry_dir, cal_entry_feed):
    for j, cal_entry in zip(xrange(len(cal_entry_feed)), cal_entry_feed):
        cal_entry_dir = os.path.join(cal_dir, entry_dir, cal_entry.title.text)
        updated = client.get_entry_updated(cal_entry)
        if os.path.exists(cal_entry_dir):
            mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime = os.stat(cal_entry_dir)
            if updated <= datetime.datetime.fromtimestamp(mtime):
                continue
        else:
            os.mkdir(cal_entry_dir)
        write(cal_entry_dir + '.self', cal_entry.GetSelfLink().href)
        if cal_entry.GetEditLink() is not None:
            write(cal_entry_dir + '.edit', cal_entry.GetEditLink().href)
        if cal_entry.content.text is not None:
            write(os.path.join(cal_entry_dir, 'content'), cal_entry.content.text)
        else:
            write(os.path.join(cal_entry_dir, 'content'), '')
        if len(cal_entry.when) > 0 and cal_entry.when[0] is not None:
            write(os.path.join(cal_entry_dir, 'when'), cal_entry.when[0].start_time + ' ' + cal_entry.when[0].end_time)
        else:
            write(os.path.join(cal_entry_dir, 'when'), '')
        if len(cal_entry.where) > 0 and cal_entry.where[0].text is not None:
            write(os.path.join(cal_entry_dir, 'where'), cal_entry.where[0].text)
        else:
            write(os.path.join(cal_entry_dir, 'where'), '') 
        if cal_entry.recurrence is not None:
            write(os.path.join(cal_entry_dir, 'recurrence'), str(cal_entry.recurrence))
        else:
            write(os.path.join(cal_entry_dir, 'recurrence'), '')
        last_updated = client.get_entry_updated_epoch(cal_entry)
        os.utime(cal_entry_dir, (last_updated, last_updated) )
       
     
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
        elif service in ['documents', 'spreadsheets', 'presentations']:
            if os.path.exists(event.path + '.edit'):
                self.client.delete_document(read(event.path + '.edit'))
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
        elif service == 'calendars':
            if os.path.exists(event.path + '.edit'):
                self.client.delete_calendar_event(read(event.path + '.edit'))
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
            photo = self.client.upload_photo(album, event.dest_path)
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
                self.client.update_contact(contact)
                write(event.dest_path + '.self', contact.GetSelfLink().href)
                if contact.GetEditLink() is not None:
                    write(event.dest_path + '.edit', contact.GetEditLink().href)
                remove_metadata(event.src_path)
            else:
                ev = ReleaseEventHandler(self.client)
                ev.consume(ReleaseEvent(event.dest_path))
        elif service == 'blogs':
            if os.path.dirname(os.path.dirname(event.src_path)).endswith('/blogs') and os.path.dirname(os.path.dirname(event.dest_path)).endswith('/blogs'):
                post = self.client.get_blog_post(read(event.src_path + '.self'))
                post.title = atom.Title('xhtml', os.path.basename(event.dest_path))
                new_post = self.client.update_blog_post(post)
                write(event.dest_path + '.self', new_post.GetSelfLink().href)
                if new_post.GetEditLink() is not None:
                    write(event.dest_path + '.edit', new_post.GetEditLink().href)
                remove_metadata(event.src_path)
            else:
                ev = ReleaseEventHandler(self.client)
                ev.consume(ReleaseEvent(event.dest_path))
        elif service in ['documents', 'spreadsheets', 'presentations']:
            doc = self.client.get_document(read(event.src_path + '.self'))
            doc.title = atom.Title('xhtml', os.path.basename(event.dest_path))
            content_type = gdata.docs.service.SUPPORTED_FILETYPES[get_file_ext(event.dest_path)]
            ms = gdata.MediaSource(file_path=event.src_path, content_type=content_type)
            new_doc = self.client.update_document(doc, ms)
            write(event.dest_path + '.self', new_doc.GetSelfLink().href)
            if new_doc.GetEditLink() is not None:
                write(event.dest_path + '.edit', new_doc.GetEditLink().href)
            remove_metadata(event.src_path)
        elif service == 'calendars':
            ev = ReleaseEventHandler(self.client)
            ev.consume(ReleaseEvent(event.dest_path))

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
        elif service == 'calendars':
            os.mkdir(event.path)
            text = os.path.basename(event.path)
            cal = self.client.get_calendar(read(os.path.dirname(event.path) + '.self'))
            if is_date_range(text):
                start, end = text.split('-')
                d1 = start[0:4] + '-' + start[4:6] + '-' + start[6:8]
                d2 = end[0:4] + '-' + end[4:6] + '-' + end[6:8]
                cal_entry_feed = self.client.calendar_query_date_range(cal, d1, d2)
            else:
                cal_entry_feed = self.client.calendar_query_full_text(cal, text)
            write_cal_entries(self.client, os.path.dirname(event.path), os.path.basename(event.path), cal_entry_feed)

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
                contact_field = os.path.basename(event.path)
            else:
                self_uri = read(os.path.dirname(os.path.dirname(event.path)) + '.self')
                contact_field = os.path.basename(os.path.dirname(event.path))
            contact = self.client.get_contact_by_uri(self_uri)
            field_val = read(event.path)
            if contact_field == 'email':
                data = gdata.contacts.Email(rel='http://schemas.google.com/g/2005#%s' % os.path.basename(event.path),primary='false',address=field_val)
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
            self.client.update_contact(contact)
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
        elif service in ['documents', 'spreadsheets', 'presentations']:
            if get_file_ext(event.path) in gdata.docs.service.SUPPORTED_FILETYPES:
                content_type = gdata.docs.service.SUPPORTED_FILETYPES[get_file_ext(event.path)]
                ms = gdata.MediaSource(file_path=event.path, content_type=content_type)
                title = os.path.basename(event.path)
                doc = self.client.upload_document(ms, title, service)
                """ for some reason updating the folder name doesn't seem to work
                if not os.path.basename(os.path.dirname(event.path)) in ['documents', 'spreadsheets', 'presentations']:
                    folder = atom.Category(scheme='http://schemas.google.com/docs/2007/folders/%s' % self.client.get_email(),term=os.path.basename(os.path.dirname(event.path)),label=os.path.basename(os.path.dirname(event.path)))
                    doc.category.append(folder)
                    ms = gdata.MediaSource(file_path=event.path, content_type=content_type)
                    self.client.update_document(doc, ms)
                """
                write(event.path + '.self', doc.GetSelfLink().href)
                if doc.GetEditLink() is not None:
                    write(event.path + '.edit', doc.GetEditLink().href)
        elif service == 'calendars':
            if os.path.basename(event.path) == 'quick':
                if os.path.exists(os.path.dirname(event.path) + '.self'):
                    cal = self.client.get_calendar(read(os.path.dirname(event.path) + '.self'))
                    content = read(event.path)
                    self.client.calendar_quick_add(cal, content)
                    os.remove(event.path)
            elif os.path.basename(event.path) == 'content':
                if os.path.exists(os.path.dirname(event.path) + '.self'):
                    cevent = self.client.get_calendar_event(read(os.path.dirname(event.path) + '.self'))
                    cevent.content = atom.Content(text=read(event.path))
                    self.client.update_calendar_event(cevent)
            elif os.path.basename(event.path) == 'when':
                if os.path.exists(os.path.dirname(event.path) + '.self'):
                    cevent = self.client.get_calendar_event(read(os.path.dirname(event.path) + '.self'))
                    start_time, end_time = read(event.path).split()
                    cevent.when[0] = gdata.calendar.When(start_time=start_time, end_time=end_time)
                    self.client.update_calendar_event(cevent)
            elif os.path.basename(event.path) == 'where':
                if os.path.exists(os.path.dirname(event.path) + '.self'):
                    cevent = self.client.get_calendar_event(read(os.path.dirname(event.path) + '.self'))
                    cevent.where[0] = gdata.calendar.Where(value_string=read(event.path))
                    self.client.update_calendar_event(cevent)
            elif os.path.basename(event.path) == 'recurrence':
                if os.path.exists(os.path.dirname(event.path) + '.self'):
                    cevent = self.client.get_calendar_event(read(os.path.dirname(event.path) + '.self'))
                    cevent.recurrence = self.client.get_recurrence_from_string(read(event.path))
                    self.client.update_calendar_event(cevent)
            
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
            try:
                self.task()
            except Exception, ex:
                logging.debug(ex)
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
                                if ex.error_code == 400 or ex.error_code == 404:
                                    remove_file_and_metadata(os.path.join(dir, entry, f))          
                    if os.path.isfile(os.path.join(dir, entry + '.self')):
                        try:
                            uri = read(os.path.join(dir, entry + '.self'))
                            album = self.client.get_album_or_photo_by_uri(uri)
                        except gdata.photos.service.GooglePhotosException, ex:
                            if ex.error_code == 400 or ex.error_code == 404:
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
                except RequestError, ex:
                    if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
                        for root, dirs, files in os.walk(os.path.join(self.contact_base_dir, contact_dir), topdown=False):
                            for file in files:
                                os.remove(os.path.join(root, file))
                        remove_dir_and_metadata(os.path.join(self.contact_base_dir, contact_dir))

class DocsCleanupThread(TaskThread):
    def __init__(self, client, docs_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 60.0
        self.docs_base_dir = docs_base_dir

    def task(self):
        dir = self.docs_base_dir
        for entry in os.listdir(dir):
            if os.path.isdir(os.path.join(dir, entry)):
                for f in os.listdir(os.path.join(dir, entry)):
                    if os.path.isfile(os.path.join(dir, entry, f + '.self')):
                        try:
                            uri = read(os.path.join(dir, entry, f + '.self'))
                            doc = self.client.get_document(uri)
                        except RequestError, ex:
                            if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
                                remove_file_and_metadata(os.path.join(dir, entry, f))          
            if os.path.isfile(os.path.join(dir, entry + '.self')):
                try:
                    uri = read(os.path.join(dir, entry + '.self'))
                    doc = self.client.get_document(uri)
                except RequestError, ex:
                    if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
                        remove_file_and_metadata(os.path.join(dir, entry))
 
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
                except RequestError, ex:
                    if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
                        for root, dirs, files in os.walk(os.path.join(self.blog_base_dir, blog_dir), topdown=False):
                            for file in files:
                                os.remove(os.path.join(root, file))
                        remove_dir_and_metadata(os.path.join(self.blog_base_dir, blog_dir))
                        continue
                for post_dir in os.listdir(os.path.join(self.blog_base_dir, blog_dir)):
                    if os.path.isfile(os.path.join(self.blog_base_dir, blog_dir, post_dir + '.self')):
                        try:
                            uri = read(os.path.join(self.blog_base_dir, blog_dir, post_dir + '.self'))
                            post = self.client.get_blog_post(uri)
                        except RequestError, ex:
                            if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
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
                updated = self.client.get_entry_updated(photo)
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
                    last_updated = self.client.get_entry_updated_epoch(photo)
                    os.utime(name, (last_updated, last_updated) )
                    write(name + '.self', photo.GetSelfLink().href)
                    if photo.GetEditLink() is not None:
                        write(name + '.edit', photo.GetEditLink().href)
            last_updated = self.client.get_entry_updated_epoch(album)
            os.utime(album_dir, (last_updated, last_updated) )

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
                updated = self.client.get_entry_updated(contact)
                contact_dir = os.path.join(self.contact_base_dir, contact.title.text)
                if os.path.exists(contact_dir):
                    mode, ino, dev, nlink, uid, gid, size, atime, mtime, ctime = os.stat(contact_dir)
                    if updated <= datetime.datetime.fromtimestamp(mtime):
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
                last_updated = self.client.get_entry_updated_epoch(contact)
                os.utime(contact_dir, (last_updated, last_updated) )
                
class DocsDownloadThread(TaskThread):
    def __init__(self, client, docs_base_dir, spreads_base_dir, presents_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self.docs_base_dir = docs_base_dir
        self.spreads_base_dir = spreads_base_dir
        self.presents_base_dir = presents_base_dir
    def task(self):
        docs_feed = self.client.docs_feed()
        for doc in docs_feed:
            if self.client.has_category(doc, 'document'):
                base_dir = self.docs_base_dir
            elif self.client.has_category(doc, 'spreadsheet'):
                base_dir = self.spreads_base_dir
            else:
                base_dir = self.presents_base_dir
            if len(self.client.get_folders(doc)) > 0:
                folder_dir = base_dir
                for folder in self.client.get_folders(doc):
                    folder_dir = os.path.join(folder_dir, folder)
                    if not os.path.exists(folder_dir):
                        os.mkdir(folder_dir)
                doc_file = os.path.join(folder_dir, doc.title.text)
            else:
                doc_file = os.path.join(base_dir, doc.title.text)
            if not os.path.exists(doc_file):
                if self.client.has_category(doc, 'document') or self.client.has_category(doc, 'presentation'):
                    write(doc_file, self.client.get_doc_content(doc))
                else:
                    write(doc_file, '')
                write(doc_file + '.self', doc.GetSelfLink().href)
                if doc.GetEditLink() is not None:
                    write(doc_file + '.edit', doc.GetEditLink().href)
                last_updated = self.client.get_entry_updated_epoch(doc)
                os.utime(doc_file, (last_updated, last_updated) )
            
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
                last_updated = self.client.get_entry_updated_epoch(post)
                os.utime(post_dir, (last_updated, last_updated) )
            last_updated = self.client.get_entry_updated_epoch(blog)
            os.utime(blog_dir, (last_updated, last_updated) )

class CalendarDownloadThread(TaskThread):
    def __init__(self, client, cal_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 100.0
        self.cal_base_dir = cal_base_dir
    def task(self):
        cal_feed = self.client.calendar_feed()
        for i, cal in zip(xrange(len(cal_feed)), cal_feed):
            cal_dir = os.path.join(self.cal_base_dir, cal.title.text)
            if not os.path.exists(cal_dir):
                os.mkdir(cal_dir)
                write(cal_dir + '.self', cal.GetSelfLink().href)
                if cal.GetEditLink() is not None:
                    write(cal_dir + '.edit', cal.GetEditLink().href)
                os.mkdir(os.path.join(cal_dir, TODAY_STR))
                os.mkdir(os.path.join(cal_dir, SEV_DAYS_STR))
                os.mkdir(os.path.join(cal_dir, THIRTY_DAYS_STR))
            today_feed = self.client.calendar_entry_feed_today(cal)
            write_cal_entries(self.client, cal_dir, TODAY_STR, today_feed)
            week_feed = self.client.calendar_entry_feed_7_days(cal)
            write_cal_entries(self.client, cal_dir, SEV_DAYS_STR, week_feed)
            month_feed = self.client.calendar_entry_feed_30_days(cal)
            write_cal_entries(self.client, cal_dir, THIRTY_DAYS_STR, month_feed)
            last_updated = self.client.get_entry_updated_epoch(cal)
            os.utime(cal_dir, (last_updated, last_updated) )

class CalendarCleanupThread(TaskThread):
    def __init__(self, client, cal_base_dir):
        TaskThread.__init__(self)
        self.client = client
        self._interval = 20.0
        self.cal_base_dir = cal_base_dir

    def task(self):
        dir = self.cal_base_dir
        for entry in os.listdir(dir):            
            if os.path.isdir(os.path.join(dir, entry)):
                if os.path.exists(os.path.join(dir, entry + '.self')):
                    cal = self.client.get_calendar(read(os.path.join(dir, entry + '.self')))
                    if cal is None:
                        remove_dir_and_metadata(os.path.join(dir, entry))
                        continue
                for f in os.listdir(os.path.join(dir, entry)):
                    if os.path.isdir(os.path.join(dir, entry, f)):
                        for e in os.listdir(os.path.join(dir, entry, f)):
                            if os.path.isdir(os.path.join(dir, entry, f, e)):
                                try:
                                    event = self.client.get_calendar_event(read(os.path.join(dir, entry, f, e + '.self')))
                                    if f == TODAY_STR:
                                        if not self.client.event_is_today(cal, event):
                                            remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                            continue
                                    elif f == SEV_DAYS_STR:
                                        if not self.client.event_is_7_days_from_now(cal, event):
                                            remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                            continue
                                    elif f == THIRTY_DAYS_STR:
                                        if not self.client.event_is_30_days_from_now(cal, event):
                                            remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                            continue
                                    elif is_date_range(f):
                                        start, end = f.split('-')
                                        d1 = start[0:4] + '-' + start[4:6] + '-' + start[6:8]
                                        d2 = end[0:4] + '-' + end[4:6] + '-' + end[6:8]
                                        if not self.client.event_matches_date_range(cal, start, end, event):
                                            remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                            continue
                                    elif not self.client.event_matches_text_query(cal, f, event):
                                        remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                        continue
                                except RequestError, ex:
                                    if ex.args[0]['status'] == 404 or ex.args[0]['status'] == 400:
                                        remove_dir_and_metadata(os.path.join(dir, entry, f, e))
                                        continue    