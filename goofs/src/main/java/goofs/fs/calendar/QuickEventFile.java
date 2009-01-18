package goofs.fs.calendar;

import com.google.gdata.data.calendar.CalendarEntry;

import goofs.calendar.ICalendar;
import goofs.fs.Dir;
import goofs.fs.SimpleFile;

public class QuickEventFile extends SimpleFile {

	public QuickEventFile(Dir parent, String name) throws Exception {

		super(parent, name);

	}

	protected ICalendar getCalendarService() {

		return ((CalendarsDir) getParent().getParent()).getCalendarService();
	}

	protected CalendarEntry getCalendar() {

		return ((CalendarDir) getParent()).getCalendar();

	}

	@Override
	public int save() {

		try {
			getCalendarService().createQuickEvent(getCalendar(),
					new String(getContent()));

			remove();

		} catch (Exception e) {

			e.printStackTrace();
		}

		return 0;

	}

}
