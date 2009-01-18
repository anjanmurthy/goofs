package goofs.fs.blogger;

import fuse.Errno;
import goofs.ServiceFactory;
import goofs.blogger.Blog;
import goofs.blogger.IBlogger;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

public class BlogsDir extends Dir {

	private IBlogger blogger;

	public BlogsDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.blogger.blogs"), 0755);

		blogger = (IBlogger) ServiceFactory.getService(IBlogger.class);

		List<Blog> blogs = blogger.getBlogs();

		for (Blog blog : blogs) {

			BlogDir dir = new BlogDir(this, blog);

			add(dir);
		}

	}

	protected IBlogger getBlogger() {
		return blogger;
	}

	protected void setBlogger(IBlogger blogger) {
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
