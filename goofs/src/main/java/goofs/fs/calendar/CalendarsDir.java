package goofs.fs.calendar;

import fuse.Errno;
import goofs.ServiceFactory;
import goofs.calendar.ICalendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

import com.google.gdata.data.calendar.CalendarEntry;

public class CalendarsDir extends Dir {

	private ICalendar calendarService;

	public CalendarsDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.calendar.calendars"),
				0755);

		calendarService = (ICalendar) ServiceFactory
				.getService(ICalendar.class);

		List<CalendarEntry> cals = calendarService.getCalendars();

		for (CalendarEntry e : cals) {
			CalendarDir dir = new CalendarDir(this, e);
			add(dir);
		}

	}

	protected ICalendar getCalendarService() {
		return calendarService;
	}

	protected void setCalendarService(ICalendar calendarService) {
		this.calendarService = calendarService;
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
