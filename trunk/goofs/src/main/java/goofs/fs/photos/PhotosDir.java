package goofs.fs.photos;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.photos.Picasa;

public class PhotosDir extends Dir {

	Picasa picasa;

	public PhotosDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.photos.photos"), 0755);

		picasa = new Picasa(System.getProperty("username"), System
				.getProperty("password"));

		PublicAlbumDir publicDir = new PublicAlbumDir(this);

		PrivateAlbumDir privateDir = new PrivateAlbumDir(this);

		add(publicDir);

		add(privateDir);
	}

	public Picasa getPicasa() {
		return picasa;
	}

	public void setPicasa(Picasa picasa) {
		this.picasa = picasa;
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
	public int delete() {
		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.EROFS;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

}
