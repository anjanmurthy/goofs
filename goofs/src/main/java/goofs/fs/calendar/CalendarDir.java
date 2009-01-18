package goofs.fs.calendar;

import fuse.Errno;
import goofs.calendar.Calendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;

public class CalendarDir extends Dir {

	protected String calendarId;

	public CalendarDir(Dir parent, CalendarEntry cal) throws Exception {

		super(parent, cal.getTitle().getPlainText(), 0755);

		setCalendarId(cal.getSelfLink().getHref());

		java.util.Calendar start = java.util.Calendar.getInstance();
		java.util.Calendar end = java.util.Calendar.getInstance();

		end.set(java.util.Calendar.HOUR_OF_DAY, 23);
		end.set(java.util.Calendar.MINUTE, 59);
		end.set(java.util.Calendar.SECOND, 59);
		end.set(java.util.Calendar.MILLISECOND, 999);

		CalendarEventDurationDir next = new CalendarEventDurationDir(this,
				"Today", cal, start.getTime(), end.getTime());

		add(next);

		start.setTime(end.getTime());
		end.add(java.util.Calendar.DATE, 6);

		next = new CalendarEventDurationDir(this, "Next 7 Days", cal, start
				.getTime(), end.getTime());

		add(next);

		start.setTime(end.getTime());
		end.add(java.util.Calendar.DATE, 23);

		next = new CalendarEventDurationDir(this, "Next 30 Days", cal, start
				.getTime(), end.getTime());

		add(next);

	}

	protected Calendar getCalendarService() {

		return ((CalendarsDir) getParent()).getCalendarService();
	}

	protected CalendarEntry getCalendar() {

		try {
			return getCalendarService().getCalendarById(getCalendarId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	protected String getCalendarId() {
		return calendarId;
	}

	protected void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (!isDir && "quick".equals(name)) {

			try {
				add(new QuickEventFile(this, name));

				return 0;

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return Errno.EROFS;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

	@Override
	public int createTempChild(String name) {

		try {
			add(new QuickEventTempFile(this, name));

			return 0;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return Errno.EROFS;

	}

	@Override
	public int delete() {

		try {
			getCalendarService().deleteCalendar(getCalendarId());

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
				CalendarEntry c = new CalendarEntry();
				c.setTitle(new PlainTextConstruct(name));
				getCalendarService().updateCalendar(getCalendarId(), c);
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
