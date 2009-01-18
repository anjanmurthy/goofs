package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.ICalendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;

public class CalendarEventDir extends Dir {

	protected String calendarEventId;

	public CalendarEventDir(Dir parent, CalendarEventEntry event)
			throws Exception {

		super(parent, event.getTitle().getPlainText(), 0755);

		setCalendarEventId(event.getSelfLink().getHref());

		if (event.getTimes() != null && !event.getTimes().isEmpty()) {
			add(new CalendarEventWhenFile(this, event));
		}

		if (event.getRecurrence() != null) {
			add(new CalendarEventRecurrenceFile(this, event));
		}
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

	protected ICalendar getCalendarService() {

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
		try {
			getCalendarService().deleteCalendarEvent(getCalendarEventId());

			remove();

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		if (getParent() == newParent) {
			try {
				CalendarEventEntry e = new CalendarEventEntry();
				e.setTitle(new PlainTextConstruct(name));
				getCalendarService().updateCalendarEvent(getCalendarEventId(),
						e);
				setName(name);
				return 0;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Errno.EROFS;
	}

}
