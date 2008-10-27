package goofs.fs.contacts;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.DiskFile;

public class ContactPhotoFile extends DiskFile {

	public ContactPhotoFile(Dir parent, String name, byte[] photo)
			throws Exception {

		super(parent, name, 0755);

		setContent(photo);

	}

	@Override
	public int save() {
		return Errno.EROFS;
	}

	@Override
	public int delete() {
		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.EROFS;
	}

}
