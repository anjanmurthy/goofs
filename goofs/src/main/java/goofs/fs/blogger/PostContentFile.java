package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Post;
import goofs.fs.Dir;
import goofs.fs.File;

public class PostContentFile extends File {

	public PostContentFile(Dir parent, Post post) throws Exception {

		super(parent, post.getEntry().getTitle().getPlainText(), 0755, post
				.getContent());

	}

	protected Blogger getBlogger() {

		BlogsDir parentDir = (BlogsDir) getParent().getParent().getParent();

		return parentDir.getBlogger();
	}

	protected Blog getBlog() {
		return ((BlogDir) getParent().getParent()).getBlog();

	}

	protected Post getPost() {

		return ((PostDir) getParent()).getPost();
	}

	@Override
	public int save() {

		try {
			getBlogger().updatePost(getPost(), getName(),
					new String(getContent()));

			return 0;
		} catch (Exception e) {
			return Errno.ENOENT;
		}
	}

	@Override
	public int delete() {
		return Errno.ENOENT;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.ENOENT;
	}

}
