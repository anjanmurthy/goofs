package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Post;
import goofs.fs.Dir;
import goofs.fs.Node;

public class PostDir extends Dir {

	private String postId;

	public PostDir(Dir parent, Post post) throws Exception {

		super(parent, post.getEntry().getTitle().getPlainText(), 0755);

		setPostId(post.getEntry().getSelfLink().getHref());

		CommentsDir commentsDir = new CommentsDir(this);

		add(commentsDir);

		PostContentFile contentFile = new PostContentFile(this, post);

		add(contentFile);

	}

	protected String getPostId() {
		return postId;
	}

	protected void setPostId(String postId) {
		this.postId = postId;
	}

	public Post getPost() {

		try {
			return getBlogger().getPostById(getPostId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}
	}

	protected Blogger getBlogger() {

		BlogsDir parentDir = (BlogsDir) getParent().getParent();

		return parentDir.getBlogger();
	}

	protected Blog getBlog() {
		return ((BlogDir) getParent()).getBlog();

	}

	@Override
	public int delete() {

		try {
			getBlogger().deletePost(getPost());

			remove();

			return 0;
		} catch (Exception e) {
			return Errno.ENOENT;
		}
	}

	@Override
	public int rename(Dir newParent, String name) {

		try {
			getBlogger().updatePost(getPost(), name, null);

			return 0;
		} catch (Exception e) {

			return Errno.ENOENT;
		}

	}

	@Override
	public int createChild(String name, boolean isDir) {

		return Errno.EROFS;

	}

	@Override
	public int createTempChild(String name) {

		try {
			PostContentTempFile f = new PostContentTempFile(this, name);

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
