package goofs.fs;

import fuse.Errno;
import goofs.fs.blogger.BlogsDir;
import goofs.fs.contacts.ContactsDir;
import goofs.fs.photos.PhotosDir;
import goofs.fs.calendar.CalendarsDir;

public class RootDir extends Dir {

	public RootDir() throws Exception {

		super(null, "", 0755, "description", "ROOT directory");

		AddDirThread t;

		t = new AddDirThread(this, BlogsDir.class);
		t.start();

		t = new AddDirThread(this, PhotosDir.class);
		t.start();

		t = new AddDirThread(this, ContactsDir.class);
		t.start();

		t = new AddDirThread(this, CalendarsDir.class);
		t.start();

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
