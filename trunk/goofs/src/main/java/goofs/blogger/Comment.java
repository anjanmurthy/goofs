package goofs.blogger;

import com.google.gdata.data.Entry;
import com.google.gdata.data.TextContent;

public class Comment {

	protected Entry entry;

	public Comment(Entry entry) {
		this.entry = entry;
	}

	public Entry getEntry() {
		return entry;
	}

	public String getContent() {

		return ((TextContent) getEntry().getContent()).getContent()
				.getPlainText();
	}
}
