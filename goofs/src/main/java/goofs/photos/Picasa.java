package goofs.photos;

import goofs.GoofsService;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaByteArraySource;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;

public class Picasa implements GoofsService {

	protected PicasawebService realService;

	public Picasa(String userName, String password)
			throws AuthenticationException {
		
		//MediaMultipart.loadMimeMappings();
		
		realService = new PicasawebService(APP_NAME);
		realService.setUserCredentials(userName, password);
	}

	protected PicasawebService getRealService() {
		return realService;
	}

	public List<AlbumEntry> getAlbums() throws Exception {

		URL feedUrl = new URL(
				"http://picasaweb.google.com/data/feed/api/user/default?kind=album");

		UserFeed myUserFeed = getRealService().getFeed(feedUrl, UserFeed.class);

		return myUserFeed.getAlbumEntries();

	}

	public AlbumEntry createAlbum(String title, String description)
			throws Exception {
		return createAlbum(title, description, false);
	}

	public AlbumEntry createAlbum(String title, String description,
			boolean isPublic) throws Exception {

		AlbumEntry myAlbum = new AlbumEntry();

		myAlbum.setTitle(new PlainTextConstruct(title));
		myAlbum.setDescription(new PlainTextConstruct(description));
		myAlbum.setAccess(isPublic ? "public" : "private");

		return getRealService()
				.insert(
						new URL(
								"http://picasaweb.google.com/data/feed/api/user/default"),
						myAlbum);
	}

	public AlbumEntry updateAlbum(AlbumEntry album, String title,
			String description) throws Exception {
		album.setTitle(new PlainTextConstruct(title));
		album.setDescription(new PlainTextConstruct(description));

		return album.update();
	}

	public void deleteAlbum(AlbumEntry album) throws Exception {

		album.delete();
	}

	protected String getAlbumId(AlbumEntry album) {
		String[] parts = album.getId().split("/");
		return parts[parts.length - 1];
	}

	public List<PhotoEntry> getPhotos(AlbumEntry album) throws Exception {

		URL feedUrl = new URL(
				"http://picasaweb.google.com/data/feed/api/user/default/albumid/"
						+ getAlbumId(album));

		AlbumFeed feed = getRealService().getFeed(feedUrl, AlbumFeed.class);

		return feed.getPhotoEntries();

	}

	public List<PhotoEntry> getPhotosByTag(String tag) throws Exception {

		URL feedUrl = new URL(
				"http://picasaweb.google.com/data/feed/api/user/default");

		Query myQuery = new Query(feedUrl);
		myQuery.setStringCustomParameter("kind", "photo");
		myQuery.setStringCustomParameter("tag", tag);

		return getRealService().query(myQuery, AlbumFeed.class)
				.getPhotoEntries();

	}

	public PhotoEntry createPhoto(AlbumEntry album, String title,
			String description, byte[] contents) throws Exception {

		URL albumPostUrl = new URL(
				"http://picasaweb.google.com/data/feed/api/user/default/albumid/"
						+ getAlbumId(album));
		PhotoEntry myPhoto = new PhotoEntry();
		myPhoto.setTitle(new PlainTextConstruct(title));
		myPhoto.setDescription(new PlainTextConstruct(description));
		myPhoto.setClient(APP_NAME);

		MediaSource myMedia = new MediaByteArraySource(contents, "image/jpeg");
		myPhoto.setMediaSource(myMedia);

		return getRealService().insert(albumPostUrl, myPhoto);
	}

	public PhotoEntry updatePhoto(PhotoEntry photo, String title,
			String description) throws Exception {

		photo.setTitle(new PlainTextConstruct(title));
		photo.setDescription(new PlainTextConstruct(description));

		return photo.update();
	}

	public PhotoEntry updatePhotoContent(PhotoEntry photo, byte[] contents)
			throws Exception {
		MediaSource myMedia = new MediaByteArraySource(contents, "image/jpeg");

		photo.setMediaSource(myMedia);

		return photo.updateMedia(true);
	}

	public byte[] getPhotoContent(PhotoEntry photo) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		URL contentUrl = new URL(photo.getMediaContents().get(0).getUrl());
		InputStream in = contentUrl.openStream();
		BufferedOutputStream out = new BufferedOutputStream(baos);
		try {

			byte[] buff = new byte[256];
			int bytesRead = 0;

			while ((bytesRead = in.read(buff)) != -1) {
				out.write(buff, 0, bytesRead);

			}
		} finally {

			out.close();
			in.close();
		}

		return baos.toByteArray();
	}

}
