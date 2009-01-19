package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.ICalendar;
import goofs.fs.Dir;
import goofs.fs.File;

import java.io.StringWriter;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.common.xml.XmlWriter;

public class CalendarEventWhereFile extends File {

	public CalendarEventWhereFile(Dir parent, CalendarEventEntry event)
			throws Exception {

		super(parent, resourceBundle.getString("goofs.calendar.where"), 0755,
				"");

		setContent(getWhere(event).getBytes());

	}

	protected ICalendar getCalendarService() {

		return ((CalendarEventDir) getParent()).getCalendarService();
	}

	protected String getWhere(CalendarEventEntry event) throws Exception {

		StringWriter sw = new StringWriter();
		XmlWriter writer = new XmlWriter(sw);

		if (event.getLocations() != null) {

			for (Where w : event.getLocations()) {

				w.generate(writer, getCalendarService().getExtensionProfile());

			}
		}

		String when = sw.getBuffer().toString();

		writer.close();

		return when;

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
