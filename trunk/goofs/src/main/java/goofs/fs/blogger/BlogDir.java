package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Post;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.fs.SimpleFile;

import java.util.List;

public class BlogDir extends Dir {

	private String blogId;

	public BlogDir(Dir parent, Blog blog) throws Exception {

		super(parent, blog.getBlogTitle(), 0755);

		setBlogId(blog.getEntry().getSelfLink().getHref());

		List<Post> posts = getBlogger().getPosts(blog);

		for (Post post : posts) {

			PostDir postDir = new PostDir(this, post);

			add(postDir);
		}

	}

	protected String getBlogId() {
		return blogId;
	}

	protected void setBlogId(String blogId) {
		this.blogId = blogId;
	}

	protected Blogger getBlogger() {

		BlogsDir parentDir = (BlogsDir) getParent();

		return parentDir.getBlogger();
	}

	public Blog getBlog() {

		try {
			return getBlogger().getBlogById(getBlogId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
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
	public int createChild(String name, boolean isDir) {

		if (!isDir)
			return Errno.EROFS;

		try {
			Post post = getBlogger().createPost(getBlog(), name, name);

			PostDir postDir = new PostDir(this, post);

			add(postDir);

			return 0;
		} catch (Exception e) {
			return Errno.EROFS;
		}

	}

	@Override
	public int createTempChild(String name) {

		try {
			SimpleFile f = new SimpleFile(this, name);

			add(f);

			return 0;

		} catch (Exception e) {
			return Errno.EROFS;
		}

	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

}
