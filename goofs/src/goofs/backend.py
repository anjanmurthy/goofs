import gdata.photos.service
import gdata.contacts.service
import gdata.docs.service
from gdata import service
from gdata.service import RequestError
from gdata.contacts import ContactEntryFromString
import datetime, time
import os
import urllib2, httplib


class GPhotosService(gdata.photos.service.PhotosService):
    def __init__(self, email, password):
        gdata.photos.service.PhotosService.__init__(self, email, password)
    def GetAuthorizationToken(self):
        return self._GetAuthToken()
    
class GContactsService(gdata.contacts.service.ContactsService):
    def __init__(self, email, password):
        gdata.contacts.service.ContactsService.__init__(self, email, password)
    def GetAuthorizationToken(self):
        return self._GetAuthToken()

class GBlogsService(service.GDataService):
    def __init__(self, email, password):
        service.GDataService.__init__(self, email, password)
        self.source = 'goofs'
        self.service = 'blogger'
        self.server = 'www.blogger.com'

class GDocumentsService(gdata.docs.service.DocsService):
    def __init__(self, email, password):
        gdata.docs.service.DocsService.__init__(self, email, password)
        self.source = 'goofs'
    def GetAuthorizationToken(self):
        return self._GetAuthToken()
    
class GClient:
    
    def __init__(self, email, password):
        self.ph_client = GPhotosService(email, password)
        self.ph_client.ProgrammaticLogin()
        self.ext_ctype = {'bmp': 'image/bmp', 'gif': 'image/gif', 'png': 'image/png', 'jpg':'image/jpeg', 'jpeg':'image/jpeg'}
        self.con_client = GContactsService(email, password)
        self.con_client.ProgrammaticLogin()
        self.blog_client = GBlogsService(email, password)
        self.blog_client.ProgrammaticLogin()
        self.docs_client = GDocumentsService(email, password)
        self.docs_client.ProgrammaticLogin()
        self.username = email.split('@')[0]
    
    def __content_type_from_path(self, path):
        parts = path.split(os.extsep)
        if len(parts) > 0:
            return self.ext_ctype[parts[-1]]
        else:
            return None
        
    def get_username(self):
        return self.username
    
    def upload_document(self, ms, title, service):
        if service == 'documents':
            return self.docs_client.UploadDocument(ms, title)
        elif service == 'spreadsheets':
            return self.docs_client.UploadSpreadsheet(ms, title)
        else:
            return self.docs_client.UploadPresentation(ms, title)
            
    def delete_document(self, uri):
        return self.docs_client.Delete(uri)
    
    def get_document(self, uri):
        return self.docs_client.Get(uri)
    
    def update_document(self, doc):
        return self.docs_client.Put(doc, doc.GetEditLink().href)
        
    def docs_feed(self):
        return self.docs_client.GetDocumentListFeed().entry
    
    def wp_feed(self):
        query = gdata.docs.service.DocumentQuery(categories=['document'])
        return self.docs_client.Query(query.ToUri()).entry
    
    def spreadsheets_feed(self):
        query = gdata.docs.service.DocumentQuery(categories=['spreadsheet'])
        return self.docs_client.Query(query.ToUri()).entry
    
    def presentations_feed(self):
        query = gdata.docs.service.DocumentQuery(categories=['presentation'])
        return self.docs_client.Query(query.ToUri()).entry
    
    def has_category(self, doc, label):
        for cat in doc.category:
            if cat.label == label:
                return True
        return False

    def get_folders(self, doc):
        folders = []
        for cat in doc.category:
            if cat.scheme is not None and cat.scheme.startswith('http://schemas.google.com/docs/2007/folders'):
                folders.append(cat.term)
        folders.reverse()
        return folders
    
    def get_doc_content(self, doc):
        request = urllib2.Request(doc.content.src)
        request.add_header('Authorization', self.docs_client.GetAuthorizationToken())
        opener = urllib2.build_opener()
        f = opener.open(request)
        thecontent = f.read()
        f.close()
        return thecontent
   
    def blogs_feed(self):
        return self.blog_client.Get('/feeds/default/blogs').entry
    
    def get_blog_id_from_uri(self, uri):
        return uri.split('/')[-1]
    
    def get_blog_id(self, blog):
        return blog.GetSelfLink().href.split('/')[-1]

    def get_post_id_from_uri(self, uri):
        return uri.split('/')[-1]
    
    def get_post_id(self, post):
        return post.GetSelfLink().href.split('/')[-1]
    
    def get_comment_id(self, comment):
        return comment.GetSelfLink().href.split('/')[-1]
    
    def create_blog_post(self, blog_id, post):
        return self.blog_client.Post(post, '/feeds/' + blog_id + '/posts/default')
    
    def create_blog_post_comment(self, blog_id, post_id, comment):
        return self.blog_client.Post(comment, '/feeds/' + blog_id + '/' + post_id + '/comments/default')
    
    def update_blog_post(self, post):
        return self.blog_client.Put(post, post.GetEditLink().href)
    
    def update_blog_comment(self, comment):
        return self.blog_client.Put(comment, comment.GetEditLink().href)
    
    def get_blog_post(self, uri):
        return self.blog_client.Get(uri)
    
    def get_blog_comment(self, uri):
        return self.blog_client.Get(uri)
    
    def get_blog(self, uri):
        return self.blog_client.Get(uri)
    
    def get_blog_posts(self, blog):
        return self.blog_client.GetFeed('/feeds/' + self.get_blog_id(blog) + '/posts/default').entry
    
    def get_post_comments(self, blog, post):
        try:
            return self.blog_client.GetFeed('/feeds/' + self.get_blog_id(blog) + '/' + self.get_post_id(post) + '/comments/default').entry
        except RequestError, e:
            return []
    
    def delete_blog_entity(self, uri):
        return self.blog_client.Delete(uri)
        
    def contacts_feed(self):
        return self.con_client.GetContactsFeed(uri='http://www.google.com/m8/feeds/contacts/default/base?max-results=1000').entry
    
    def get_contact_by_uri(self, uri):
        return self.con_client.Get(uri, converter=gdata.contacts.ContactEntryFromString)
    
    def delete_contact(self, uri):
        return self.con_client.DeleteContact(uri)
    
    def update_contact(self, uri, contact):
        return self.con_client.UpdateContact(uri, contact)
    
    def upload_contact(self, contact):
        return self.con_client.CreateContact(contact)
    
    def albums_feed(self):
        return self.ph_client.GetUserFeed().entry

    def get_album_or_photo_by_uri(self, uri):
        return self.ph_client.GetEntry(uri)
    
    def photos_feed(self, album):
        return self.ph_client.GetFeed(album.GetPhotosUri()).entry

    def upload_photo(self, album, path):    
        return self.ph_client.InsertPhotoSimple(album, os.path.basename(path), os.path.basename(path), path, self.__content_type_from_path(path))

    def upload_photo_with_path(self, album, src_path, dest_path):    
        return self.ph_client.InsertPhotoSimple(album, os.path.basename(dest_path), os.path.basename(dest_path), src_path, self.__content_type_from_path(src_path))

    def upload_photo_blob(self, photo, path):    
        return self.ph_client.UpdatePhotoBlob(photo, path, self.__content_type_from_path(path))
    
    def update_photo_meta(self, photo):
        return self.ph_client.UpdatePhotoMetadata(photo)

    def upload_album(self, album_name, pub_or_priv):
        return self.ph_client.InsertAlbum(album_name, album_name, access=pub_or_priv)
    
    def get_photo(self, uri):
        return self.ph_client.GetEntry(uri)
    
    def delete_album_or_photo_by_uri(self, uri):
        return self.ph_client.Delete(uri)

    def delete_album(self, album):
        return self.ph_client.Delete(album)

    def get_photo_content(self, photo):
        request = urllib2.Request(photo.media.content[0].url)
        request.add_header('Authorization', self.ph_client.GetAuthorizationToken())
        opener = urllib2.build_opener()
        f = opener.open(request)
        thecontent = f.read()
        f.close()
        return thecontent
    
    def get_entry_updated_epoch(self, entry):
        t = datetime.datetime(*time.strptime(self.get_entry_updated_str(entry)[0:19], '%Y-%m-%dT%H:%M:%S')[0:5])
        return int(time.mktime(t.timetuple()))

    def get_entry_updated_str(self, entry):
        return entry.updated.text
    
    def get_entry_updated(self, entry):
        return datetime.datetime.strptime(self.get_entry_updated_str(entry)[0:19], '%Y-%m-%dT%H:%M:%S')
    
    def search_photos_feed(self, query):
        return self.ph_client.SearchUserPhotos(query).entry
