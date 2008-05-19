import getpass
import os
import datetime
import gdata.photos.service
from gdata.photos import AlbumEntry
from gdata.photos import PhotoEntry

import unittest

IMAGE = '/home/rwynn/Pictures/einstein.jpeg'
ALBUM = 'Einstein Album'

class GDataPythonTestCase(unittest.TestCase):
	def setUp(self):
		user = None
		pw = None
        
		while not user:
			user = raw_input('Please enter your username: ')
		while not pw:
			pw = getpass.getpass()
			if not pw:
				print 'Password cannot be blank.'
		
		self.client = gdata.photos.service.PhotosService(user, pw)
		self.client.ProgrammaticLogin()

	def tearDown(self):
		self.client = None
		
class UploadTestCase(GDataPythonTestCase):

	def runTest(self):
		try:
			album = self.client.InsertAlbum(ALBUM, ALBUM, access='public')
			assert(album is not None and isinstance(album, gdata.photos.AlbumEntry))
			photo = self.client.InsertPhotoSimple(album, os.path.basename(IMAGE), os.path.basename(IMAGE), IMAGE, 'image/jpeg')
			assert(photo is not None and isinstance(photo, gdata.photos.PhotoEntry))
		except GooglePhotosException, ex:
			self.fail(ex)

if __name__ == "__main__":
	unittest.main()	
			
	
		