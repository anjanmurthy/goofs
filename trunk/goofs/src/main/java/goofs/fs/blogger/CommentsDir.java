package goofs.fs.blogger;

import java.util.List;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Comment;
import goofs.blogger.IBlogger;
import goofs.blogger.Post;
import goofs.fs.Dir;
import goofs.fs.SimpleDir;
import goofs.fs.SimpleFile;

public class CommentsDir extends SimpleDir {

	public CommentsDir(Dir parent) {
		super(parent, resourceBundle.getString("goofs.blogger.comments"));

		try {
			List<Comment> comments = getBlogger().getComments(getBlog(),
					getPost());

			for (Comment comment : comments) {

				CommentFile commentFile = new CommentFile(this, comment);

				add(commentFile);

			}
		} catch (Exception e) {

		}

	}

	protected IBlogger getBlogger() {

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

}
