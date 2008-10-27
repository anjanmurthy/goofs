package goofs.fs.photos;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.DiskFile;
import goofs.photos.Picasa;

import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;

public class PhotoFile extends DiskFile {

	protected PhotoEntry photo;

	public PhotoFile(Dir parent, PhotoEntry photo) throws Exception {

		super(parent, photo.getTitle().getPlainText(), 0755);

		try {
			setContent(getPicasa().getPhotoContent(photo));
		} catch (Exception e) {
		}

		setPhoto(photo);

	}

	public PhotoFile(Dir parent, String name) throws Exception {

		super(parent, name, 0755);

	}

	public PhotoEntry getPhoto() {
		return photo;
	}

	public void setPhoto(PhotoEntry photo) {
		this.photo = photo;
	}

	protected Picasa getPicasa() {

		return ((PhotosDir) getParent().getParent().getParent()).getPicasa();
	}

	protected AlbumEntry getAlbum() {

		return ((AlbumDir) getParent()).getAlbum();
	}

	@Override
	public int save() {
		try {

			if (getPhoto() == null) {
				setPhoto(getPicasa().createPhoto(getAlbum(), name, name,
						getContent()));
			} else {
				setPhoto(getPicasa().updatePhotoContent(getPhoto(),
						getContent()));
			}

			return 0;

		} catch (Exception e) {
			e.printStackTrace();

			if (getPhoto() == null) {
				remove();
			}

			return Errno.ENOENT;

		}

	}

	@Override
	public int delete() {

		try {
			getPhoto().delete();

			remove();

			return 0;
		} catch (Exception e) {
			return Errno.ENOENT;
		}

	}

	@Override
	public int rename(Dir newParent, String name) {

		try {
			if (getParent() == newParent) {

				setPhoto(getPicasa().updatePhoto(getPhoto(), name, name));

				setName(name);

			}

			else {

				int result = newParent.createChildFromExisting(name, this);

				if (result == 0) {
					return delete();
				}

				else {
					return result;
				}

			}

			return 0;

		} catch (Exception e) {
			return Errno.ENOENT;
		}

	}
}
