package goofs.fs.photos;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.SimpleDir;
import goofs.photos.Picasa;

import java.util.List;

import com.google.gdata.data.photos.AlbumEntry;

public class PublicAlbumDir extends SimpleDir {

	public PublicAlbumDir(Dir parent) throws Exception {
		super(parent, resourceBundle.getString("goofs.photos.public"));

		List<AlbumEntry> albums = getPicasa().getAlbums();

		for (AlbumEntry album : albums) {

			if ("public".equals(album.getAccess())) {

				add(new AlbumDir(this, album));
			}

		}
	}

	protected Picasa getPicasa() {

		return ((PhotosDir) getParent()).getPicasa();
	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (!isDir)
			return Errno.EROFS;

		try {

			AlbumEntry album = getPicasa().createAlbum(name, name, true);

			add(new AlbumDir(this, album));

			return 0;
		} catch (Exception e) {
			return Errno.EROFS;

		}
	}

}
