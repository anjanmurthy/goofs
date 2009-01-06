package goofs.blogger;

import goofs.GoofsService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.client.blogger.BloggerService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.util.AuthenticationException;

public class Blogger implements GoofsService {

	protected BloggerService realService;

	public Blogger(String userName, String password)
			throws AuthenticationException {

		realService = new BloggerService(APP_NAME);
		realService.setUserCredentials(userName, password);
	}

	protected BloggerService getRealService() {
		return realService;
	}

	public List<Blog> getBlogs() throws Exception {
		List<Blog> blogs = new ArrayList<Blog>();

		final URL feedUrl = new URL(
				"http://www.blogger.com/feeds/default/blogs");
		Feed resultFeed = getRealService().getFeed(feedUrl, Feed.class);

		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			Entry entry = resultFeed.getEntries().get(i);
			blogs.add(new Blog(entry));
		}

		return blogs;
	}

	public Blog getBlogById(String blogId) throws Exception {

		return new Blog(getRealService().getEntry(new URL(blogId), Entry.class));

	}

	public Post getPostById(String postId) throws Exception {

		return new Post(getRealService().getEntry(new URL(postId), Entry.class));

	}

	public Comment getCommentById(String commentId) throws Exception {

		return new Comment(getRealService().getEntry(new URL(commentId),
				Entry.class));
	}

	public List<Post> getPosts(Blog blog) throws Exception {

		return getPosts(blog, null);

	}

	public List<Post> getPosts(Blog blog, Query query) throws Exception {
		List<Post> posts = new ArrayList<Post>();

		URL feedUrl = new URL("http://www.blogger.com/feeds/"
				+ blog.getBlogId() + "/posts/default");

		Feed resultFeed = (query != null) ? getRealService().query(query,
				Feed.class) : getRealService().getFeed(feedUrl, Feed.class);

		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			Entry entry = resultFeed.getEntries().get(i);
			posts.add(new Post(entry));
		}

		return posts;

	}

	public List<Post> getPosts(Blog blog, DateTime start, DateTime end)
			throws Exception {

		URL feedUrl = new URL("http://www.blogger.com/feeds/"
				+ blog.getBlogId() + "/posts/default");
		Query myQuery = new Query(feedUrl);
		myQuery.setPublishedMin(start);
		myQuery.setPublishedMax(end);

		return getPosts(blog, myQuery);
	}

	public Post createPost(Blog blog, String title, String content)
			throws Exception {
		return createPost(blog, title, content, false);
	}

	public Post createPost(Blog blog, String title, String content,
			boolean isDraft) throws Exception {
		// Create the entry to insert
		Entry myEntry = new Entry();
		myEntry.setTitle(new PlainTextConstruct(title));
		myEntry.setContent(new PlainTextConstruct(content));
		myEntry.setDraft(isDraft);

		// Ask the service to insert the new entry
		URL postUrl = new URL("http://www.blogger.com/feeds/"
				+ blog.getBlogId() + "/posts/default");
		return new Post(getRealService().insert(postUrl, myEntry));
	}

	public Post updatePost(Post post, String title, String content)
			throws Exception {

		if (title != null)
			post.getEntry().setTitle(new PlainTextConstruct(title));

		if (content != null)
			post.getEntry().setContent(new PlainTextConstruct(content));

		URL editUrl = new URL(post.getEntry().getEditLink().getHref());

		return new Post(getRealService().update(editUrl, post.getEntry()));

	}

	public void deletePost(Post post) throws Exception {

		getRealService().delete(
				new URL(post.getEntry().getEditLink().getHref()));

	}

	public Comment createComment(Blog blog, Post post, String comment)
			throws Exception {

		String commentsFeedUri = "http://www.blogger.com/feeds/"
				+ blog.getBlogId() + "/" + post.getPostId()
				+ "/comments/default";
		URL feedUrl = new URL(commentsFeedUri);

		Entry myEntry = new Entry();
		myEntry.setContent(new PlainTextConstruct(comment));

		return new Comment(getRealService().insert(feedUrl, myEntry));
	}

	public List<Comment> getComments(Blog blog, Post post) throws Exception {
		List<Comment> comments = new ArrayList<Comment>();
		String commentsFeedUri = "http://www.blogger.com/feeds/"
				+ blog.getBlogId() + "/" + post.getPostId()
				+ "/comments/default";
		URL feedUrl = new URL(commentsFeedUri);
		Feed resultFeed = getRealService().getFeed(feedUrl, Feed.class);
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			Entry entry = resultFeed.getEntries().get(i);
			comments.add(new Comment(entry));
		}
		return comments;
	}

	public void deleteComment(Comment comment) throws Exception {

		getRealService().delete(
				new URL(comment.getEntry().getEditLink().getHref()));
	}

	public Comment updateComment(Comment comment, String content)
			throws Exception {

		if (content != null) {
			comment.getEntry().setContent(new PlainTextConstruct(content));
		}

		URL editUrl = new URL(comment.getEntry().getEditLink().getHref());

		return new Comment(getRealService().update(editUrl, comment.getEntry()));

	}

}
