package goofs.fs.calendar;

import com.google.gdata.data.calendar.CalendarEventEntry;

import fuse.Errno;
import goofs.GoofsProperties;
import goofs.fs.Dir;
import goofs.fs.File;

public class CalendarEventSummaryFile extends File {

	public CalendarEventSummaryFile(Dir parent, CalendarEventEntry event)
			throws Exception {

		super(parent, GoofsProperties.INSTANCE
				.getProperty("goofs.calendar.summary"), 0755, event
				.getSummary().getPlainText());

	}

	@Override
	public int save() {
		return Errno.EROFS;
	}

	@Override
	public int delete() {
		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.EROFS;
	}

}
