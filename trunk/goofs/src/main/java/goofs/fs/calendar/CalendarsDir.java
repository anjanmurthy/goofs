package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

import com.google.gdata.data.calendar.CalendarEntry;

public class CalendarsDir extends Dir {

	private Calendar calendarService;

	public CalendarsDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.calendar.calendars"),
				0755);

		calendarService = new Calendar(System.getProperty("username"), System
				.getProperty("password"));

		List<CalendarEntry> cals = calendarService.getCalendars();

		for (CalendarEntry e : cals) {
			CalendarDir dir = new CalendarDir(this, e);
			add(dir);
		}

	}

	protected Calendar getCalendarService() {
		return calendarService;
	}

	protected void setCalendarService(Calendar calendarService) {
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
