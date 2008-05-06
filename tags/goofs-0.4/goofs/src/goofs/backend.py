import gdata.photos.service
import gdata.contacts.service
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
    
class GClient:
    
    def __init__(self, email, password):
        self.ph_client = GPhotosService(email, password)
        self.ph_client.ProgrammaticLogin()
        self.ext_ctype = {'bmp': 'image/bmp', 'gif': 'image/gif', 'png': 'image/png', 'jpg':'image/jpeg', 'jpeg':'image/jpeg'}
        self.con_client = GContactsService(email, password)
        self.con_client.ProgrammaticLogin()
        self.blog_client = GBlogsService(email, password)
        self.blog_client.ProgrammaticLogin()
        self.username = email.split('@')[0]
    
    def __content_type_from_path(self, path):
        parts = path.split(os.extsep)
        if len(parts) > 0:
            return self.ext_ctype[parts[-1]]
        else:
            return None
        
    def get_username(self):
        return self.username
    
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
    
    def get_contact_updated(self, contact):
        return datetime.datetime.strptime(self.get_contact_updated_str(contact)[0:19], '%Y-%m-%dT%H:%M:%S')

    def get_contact_updated_str(self, contact):
        return contact.updated.text
    
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
    
    def get_photo_updated(self, photo):
        return datetime.datetime.strptime(self.get_photo_updated_str(photo)[0:19], '%Y-%m-%dT%H:%M:%S')

    def get_photo_updated_str(self, photo):
        return photo.updated.text
    
    def search_photos_feed(self, query):
        return self.ph_client.SearchUserPhotos(query).entry
