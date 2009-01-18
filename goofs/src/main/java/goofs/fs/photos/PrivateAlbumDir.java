package goofs.fs.photos;

import fuse.Errno;
import goofs.fs.Dir;
import goofs.fs.SimpleDir;
import goofs.photos.IPicasa;

import java.util.List;

import com.google.gdata.data.photos.AlbumEntry;

public class PrivateAlbumDir extends SimpleDir {

	public PrivateAlbumDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.photos.private"));

		List<AlbumEntry> albums = getPicasa().getAlbums();

		for (AlbumEntry album : albums) {

			if ("private".equals(album.getAccess())) {

				add(new AlbumDir(this, album));
			}

		}

	}

	protected IPicasa getPicasa() {

		return ((PhotosDir) getParent()).getPicasa();
	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (!isDir)
			return Errno.EROFS;

		try {

			AlbumEntry album = getPicasa().createAlbum(name, name, false);

			add(new AlbumDir(this, album));

			return 0;
		} catch (Exception e) {
			return Errno.EROFS;

		}
	}
}
