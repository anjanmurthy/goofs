package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.File;

import java.io.StringWriter;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.common.xml.XmlWriter;

public class CalendarEventWhenFile extends File {

	public CalendarEventWhenFile(Dir parent, CalendarEventEntry event)
			throws Exception {

		super(parent, resourceBundle.getString("goofs.calendar.when"), 0755, "");

		setContent(getWhen(event).getBytes());

	}

	protected Calendar getCalendarService() {

		return ((CalendarEventDir) getParent()).getCalendarService();
	}

	protected String getWhen(CalendarEventEntry event) throws Exception {

		StringWriter sw = new StringWriter();
		XmlWriter writer = new XmlWriter(sw);

		if (event.getTimes() != null) {

			for (When w : event.getTimes()) {

				w.generate(writer, getCalendarService().getExtensionProfile());

				break;
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
