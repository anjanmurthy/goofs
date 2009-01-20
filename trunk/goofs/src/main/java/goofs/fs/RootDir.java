package goofs.fs;

import fuse.Errno;
import goofs.fs.blogger.BlogsDir;
import goofs.fs.calendar.CalendarsDir;
import goofs.fs.contacts.ContactsDir;
import goofs.fs.photos.PhotosDir;

public class RootDir extends Dir implements ResourceAware {

	public RootDir() throws Exception {

		super(null, "", 0755, "description", "ROOT directory");

		AddDirThread t;

		if (Boolean.TRUE.toString().equals(
				resourceBundle.getString("goofs.blogger.enabled"))) {

			t = new AddDirThread(this, BlogsDir.class);
			t.start();
		}

		if (Boolean.TRUE.toString().equals(
				resourceBundle.getString("goofs.photos.enabled"))) {
			t = new AddDirThread(this, PhotosDir.class);
			t.start();
		}

		if (Boolean.TRUE.toString().equals(
				resourceBundle.getString("goofs.contacts.enabled"))) {
			t = new AddDirThread(this, ContactsDir.class);
			t.start();
		}

		if (Boolean.TRUE.toString().equals(
				resourceBundle.getString("goofs.calendar.enabled"))) {
			t = new AddDirThread(this, CalendarsDir.class);
			t.start();
		}

	}

	@Override
	public int delete() {
		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.EROFS;
	}

	@Override
	public void remove() {
	}

	@Override
	public int createChild(String name, boolean isDir) {
		return Errno.EROFS;
	}

	@Override
	public int createTempChild(String name) {
		return Errno.EROFS;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

}
