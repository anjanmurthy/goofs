package goofs.fs.calendar;

import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;

public class CalendarEventByTextDir extends Dir {

	public CalendarEventByTextDir(Dir parent, String name, CalendarEntry cal)
			throws Exception {

		super(parent, name, 0755);

		List<CalendarEventEntry> events = getCalendarService().getEvents(cal,
				getName());

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

	@Override
	public int createChild(String name, boolean isDir) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int createTempChild(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int rename(Dir newParent, String name) {

		if (getParent() == newParent) {

			((CalendarDir) parent).createChild(name, true);

			remove();

		}

		return 0;
	}

}
