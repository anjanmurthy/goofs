package goofs.blogger;

import goofs.AbstractGoogleService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.Query;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.util.AuthenticationException;

public class Blogger extends AbstractGoogleService {

	public Blogger(String userName, String password)
			throws AuthenticationException {
		super(userName, password);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getGoogleServiceName() {
		return "blogger";
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

}
