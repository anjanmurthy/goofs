package goofs.fs;

import fuse.Errno;

public class SimpleFile extends File {

	public SimpleFile(Dir parent, String name) throws Exception {

		super(parent, name, 0755, "");
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
