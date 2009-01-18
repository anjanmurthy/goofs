package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.Date;
import java.util.List;

import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;

public class CalendarEventDurationDir extends Dir {

	protected Date start;

	protected Date end;

	public CalendarEventDurationDir(Dir parent, String name, CalendarEntry cal,
			Date start, Date end) throws Exception {

		super(parent, name, 0755);

		setStart(start);

		setEnd(end);

		List<CalendarEventEntry> events = getCalendarService().getEvents(cal,
				start, end);

		for (CalendarEventEntry event : events) {

			CalendarEventDir eventDir = new CalendarEventDir(this, event);

			add(eventDir);
		}

	}

	protected Calendar getCalendarService() {

		return ((CalendarsDir) getParent().getParent()).getCalendarService();
	}

	protected CalendarEntry getCalendar() {

		return ((CalendarDir) getParent()).getCalendar();

	}

	protected Date getStart() {
		return start;
	}

	protected void setStart(Date start) {
		this.start = start;
	}

	protected Date getEnd() {
		return end;
	}

	protected void setEnd(Date end) {
		this.end = end;
	}

	@Override
	public int createChild(String name, boolean isDir) {
		return Errno.EROFS;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

	@Override
	public int createTempChild(String name) {
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
