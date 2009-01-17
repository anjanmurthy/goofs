package goofs.fs.blogger;

import fuse.Errno;
import goofs.Fetchable;
import goofs.Identifiable;
import goofs.NotFoundException;
import goofs.blogger.Blog;
import goofs.blogger.Blogger;
import goofs.blogger.Comment;
import goofs.fs.Dir;
import goofs.fs.File;

public class CommentFile extends File implements Identifiable, Fetchable {

	private String commentId;

	public CommentFile(Dir parent, Comment comment) throws Exception {

		super(parent, comment.getEntry().getTitle().getPlainText(), 0755,
				comment.getContent());

		setCommentId(comment.getEntry().getSelfLink().getHref());
	}

	public String getId() {
		return getCommentId();
	}

	protected String getCommentId() {
		return commentId;
	}

	protected void setCommentId(String commentId) {
		this.commentId = commentId;
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

		try {
			return getBlogger().getCommentById(getCommentId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Object fetch() throws NotFoundException {
		Object o = getComment();
		if (o == null) {
			throw new NotFoundException(toString());
		}
		return o;
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

		try {

			getBlogger().updateComment(getComment(), new String(getContent()));

			return 0;
		} catch (Exception e) {
			return Errno.ENOENT;
		}

	}
}
