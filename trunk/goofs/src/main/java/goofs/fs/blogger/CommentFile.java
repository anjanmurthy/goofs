package goofs.fs.blogger;

import fuse.Errno;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Comment;
import goofs.fs.Dir;
import goofs.fs.File;

public class CommentFile extends File {

	protected Comment comment;

	public CommentFile(Dir parent, Comment comment) throws Exception {

		super(parent, comment.getEntry().getTitle().getPlainText(), 0755,
				comment.getContent());

		this.comment = comment;
	}

	protected Blogger getBlogger() {

		BlogsDir parentDir = (BlogsDir) getParent().getParent().getParent()
				.getParent();

		return parentDir.getBlogger();
	}

	protected Blog getBlog() {
		return ((BlogDir) getParent().getParent().getParent()).getBlog();

	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	@Override
	public int delete() {

		try {
			getBlogger().deleteComment(getComment());

			remove();

			return 0;
		} catch (Exception e) {

			return Errno.EROFS;
		}
	}

	@Override
	public int rename(Dir newParent, String name) {

		return Errno.EROFS;
	}

	@Override
	public int save() {

		return Errno.EROFS;

	}
}
