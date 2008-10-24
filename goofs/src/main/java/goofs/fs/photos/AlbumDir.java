package goofs.fs.photos;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.fs.SimpleFile;
import goofs.photos.Picasa;

import java.util.List;

import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.VersionConflictException;

public class AlbumDir extends Dir {

	private AlbumEntry album;

	public AlbumDir(Dir parent, AlbumEntry album) throws Exception {

		super(parent, album.getTitle().getPlainText(), 0755);

		this.album = album;

		List<PhotoEntry> photos = getPicasa().getPhotos(album);

		for (PhotoEntry photo : photos) {

			PhotoFile photoFile = new PhotoFile(this, photo);

			add(photoFile);
		}
	}

	protected Picasa getPicasa() {

		return ((PhotosDir) getParent().getParent()).getPicasa();
	}

	public AlbumEntry getAlbum() {
		return album;
	}

	public void setAlbum(AlbumEntry album) {
		this.album = album;
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
				setAlbum(getPicasa().updateAlbum(getAlbum(), name, name));

				setName(name);

				return 0;
			} catch (VersionConflictException e) {

				try {
					List<AlbumEntry> albums = getPicasa().getAlbums();

					for (AlbumEntry album : albums) {

						if (album.getId().equals(getAlbum().getId())) {

							setAlbum(getPicasa().updateAlbum(album, name, name));

							setName(name);

							return 0;
						}

					}

					return Errno.EROFS;

				} catch (Exception e1) {
					return Errno.EROFS;
				}

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
