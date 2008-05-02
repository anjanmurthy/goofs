import gdata.photos.service
import gdata.contacts.service
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
    
class GClient:
    
    def __init__(self, email, password):
        self.ph_client = GPhotosService(email, password)
        self.ph_client.ProgrammaticLogin()
        self.ext_ctype = {'bmp': 'image/bmp', 'gif': 'image/gif', 'png': 'image/png', 'jpg':'image/jpeg', 'jpeg':'image/jpeg'}
        self.con_client = GContactsService(email, password)
        self.con_client.ProgrammaticLogin()
        self.username = email.split('@')[0]
    
    def __content_type_from_path(self, path):
        parts = path.split(os.extsep)
        if len(parts) > 0:
            return self.ext_ctype[parts[-1]]
        else:
            return None
        
    def get_username(self):
        return self.username
        
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
