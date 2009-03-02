package goofs.calendar;

import goofs.GoofsService;

import java.util.Date;
import java.util.List;

import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;

public interface ICalendar extends GoofsService {

	public abstract List<CalendarEntry> getCalendars() throws Exception;

	public abstract CalendarEntry createCalendar(String title) throws Exception;

	public abstract CalendarEntry getCalendarById(String id) throws Exception;

	public abstract CalendarEventEntry getCalendarEventById(String id)
			throws Exception;

	public abstract CalendarEntry updateCalendar(String id, CalendarEntry in)
			throws Exception;

	public abstract CalendarEventEntry updateCalendarEvent(String id,
			CalendarEventEntry in) throws Exception;

	public abstract void deleteCalendar(String id) throws Exception;

	public abstract void deleteCalendarEvent(String id) throws Exception;

	public abstract CalendarEntry subscribeToCalendar(String id)
			throws Exception;

	public abstract List<CalendarEventEntry> getEvents(CalendarEntry cal)
			throws Exception;

	public abstract List<CalendarEventEntry> getEvents(CalendarEntry cal,
			Date start, Date end) throws Exception;

	public abstract List<CalendarEventEntry> getEvents(CalendarEntry cal,
			String query) throws Exception;

	public abstract CalendarEventEntry createQuickEvent(CalendarEntry cal,
			String event) throws Exception;

	public abstract ExtensionProfile getExtensionProfile();

}