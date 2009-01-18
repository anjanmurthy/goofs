package goofs.fs.calendar;

import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import com.google.gdata.data.calendar.CalendarEventEntry;

public class CalendarEventDir extends Dir {

	protected String calendarEventId;

	public CalendarEventDir(Dir parent, CalendarEventEntry event)
			throws Exception {

		super(parent, event.getTitle().getPlainText(), 0755);

		setCalendarEventId(event.getSelfLink().getHref());

	}

	protected CalendarEventEntry getCalendarEvent() {

		try {
			return getCalendarService().getCalendarEventById(
					getCalendarEventId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	protected Calendar getCalendarService() {

		return ((CalendarsDir) getParent().getParent().getParent())
				.getCalendarService();
	}

	protected String getCalendarEventId() {
		return calendarEventId;
	}

	protected void setCalendarEventId(String calendarEventId) {
		this.calendarEventId = calendarEventId;
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
		// TODO Auto-generated method stub
		return 0;
	}

}
