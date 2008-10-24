package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

public class BlogsDir extends Dir {

	private Blogger blogger;

	public BlogsDir(Dir parent) throws Exception {

		super(parent, "blogs", 0755);

		blogger = new Blogger(System.getProperty("username"), System
				.getProperty("password"));

		List<Blog> blogs = blogger.getBlogs();

		for (Blog blog : blogs) {

			BlogDir dir = new BlogDir(this, blog);

			add(dir);
		}

	}

	public Blogger getBlogger() {
		return blogger;
	}

	public void setBlogger(Blogger blogger) {
		this.blogger = blogger;
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
