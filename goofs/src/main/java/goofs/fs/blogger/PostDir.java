package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Comment;
import goofs.blogger.Post;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.fs.SimpleFile;

public class PostDir extends Dir {

	private Post post;

	public PostDir(Dir parent, Post post) throws Exception {

		super(parent, post.getEntry().getTitle().getPlainText(), 0755);

		this.post = post;

		CommentsDir commentsDir = new CommentsDir(this);

		add(commentsDir);

		PostContentFile contentFile = new PostContentFile(this, post);

		add(contentFile);

	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
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
			setPost(getBlogger().updatePost(getPost(), name, null));

			return 0;
		} catch (Exception e) {

			return Errno.ENOENT;
		}

	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (isDir)
			return Errno.EROFS;

		try {
			Comment comment = getBlogger().createComment(getBlog(), getPost(),
					name);

			CommentFile commentFile = new CommentFile(this, comment);

			add(commentFile);

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
