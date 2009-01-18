package goofs.fs.photos;

import fuse.Errno;
import goofs.Fetchable;
import goofs.Identifiable;
import goofs.NotFoundException;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.fs.SimpleFile;
import goofs.photos.IPicasa;

import java.util.List;

import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;

public class AlbumDir extends Dir implements Identifiable, Fetchable {

	protected String albumId;

	public AlbumDir(Dir parent, AlbumEntry album) throws Exception {

		super(parent, album.getTitle().getPlainText(), 0755);

		setAlbumId(album.getId());

		List<PhotoEntry> photos = getPicasa().getPhotos(album);

		for (PhotoEntry photo : photos) {

			PhotoFile photoFile = new PhotoFile(this, photo);

			add(photoFile);
		}
	}

	public String getId() {
		return getAlbumId();
	}

	protected String getAlbumId() {
		return albumId;
	}

	protected void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	protected IPicasa getPicasa() {

		return ((PhotosDir) getParent().getParent()).getPicasa();
	}

	public AlbumEntry getAlbum() {
		try {
			return getPicasa().getAlbumById(getAlbumId());
		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}
	}

	public Object fetch() throws NotFoundException {

		Object o = getAlbum();
		if (o == null) {
			throw new NotFoundException(toString());
		}
		return o;
	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (isDir)
			return Errno.EROFS;
		try {

			PhotoFile photoFile = new PhotoFile(this, name);

			add(photoFile);

			return 0;

		} catch (Exception e) {

			e.printStackTrace();

			return Errno.EROFS;
		}
	}

	@Override
	public int createTempChild(String name) {
		try {
			SimpleFile f = new SimpleFile(this, name);

			add(f);

			return 0;

		} catch (Exception e) {

			e.printStackTrace();

			return Errno.EROFS;
		}
	}

	@Override
	public int delete() {

		try {
			getPicasa().deleteAlbum(getAlbum());

			remove();

			return 0;
		} catch (Exception e) {

			return Errno.EROFS;
		}
	}

	@Override
	public int rename(Dir newParent, String name) {

		if (newParent == getParent()) {
			try {
				getPicasa().updateAlbum(getAlbum(), name, name);

				setName(name);

				return 0;
			}

			catch (Exception e) {

				e.printStackTrace();

				return Errno.EROFS;
			}
		} else {
			return Errno.EACCES;
		}
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		try {
			if (child instanceof PhotoFile) {

				PhotoFile photoFile = (PhotoFile) child;

				PhotoEntry newPhoto = getPicasa().createPhoto(getAlbum(), name,
						name, photoFile.getContent());

				PhotoFile newChild = new PhotoFile(this, newPhoto);

				add(newChild);

				return 0;
			} else {
				return Errno.EACCES;
			}
		} catch (Exception e) {
			return Errno.EROFS;
		}
	}

}
