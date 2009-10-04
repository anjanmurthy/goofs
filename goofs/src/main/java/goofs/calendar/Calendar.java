package goofs.calendar;

import java.net.URL;
import java.util.Date;
import java.util.List;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.Reminder.Method;
import com.google.gdata.util.AuthenticationException;

public class Calendar implements ICalendar {

	protected CalendarService realService;

	public Calendar(String userName, String password)
			throws AuthenticationException {
		realService = new CalendarService(APP_NAME);
		realService.setUserCredentials(userName, password);
	}

	public CalendarService getRealService() {
		return realService;
	}

	public void acquireSessionTokens(String username, String password)
			throws AuthenticationException {

		getRealService().setUserCredentials(username, password);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#getCalendars()
	 */
	public List<CalendarEntry> getCalendars() throws Exception {

		URL feedUrl = new URL(
				"http://www.google.com/calendar/feeds/default/allcalendars/full");
		CalendarFeed resultFeed = getRealService().getFeed(feedUrl,
				CalendarFeed.class);
		return resultFeed.getEntries();

	}

	protected String getCalendarId(CalendarEntry entry) {
		String[] parts = entry.getId().split("/");
		return parts[parts.length - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#createCalendar(java.lang.String)
	 */
	public CalendarEntry createCalendar(String title) throws Exception {

		CalendarEntry calendar = new CalendarEntry();
		calendar.setTitle(new PlainTextConstruct(title));
		URL postUrl = new URL(
				"http://www.google.com/calendar/feeds/default/owncalendars/full");
		return getRealService().insert(postUrl, calendar);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#getCalendarById(java.lang.String)
	 */
	public CalendarEntry getCalendarById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), CalendarEntry.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#getCalendarEventById(java.lang.String)
	 */
	public CalendarEventEntry getCalendarEventById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), CalendarEventEntry.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#updateCalendar(java.lang.String,
	 * com.google.gdata.data.calendar.CalendarEntry)
	 */
	public CalendarEntry updateCalendar(String id, CalendarEntry in)
			throws Exception {

		CalendarEntry current = getCalendarById(id);

		if (in.getTitle() != null) {
			current.setTitle(in.getTitle());
		}

		if (in.getSummary() != null) {
			current.setSummary(in.getSummary());
		}

		if (in.getAccessLevel() != null) {
			current.setAccessLevel(in.getAccessLevel());
		}

		if (in.getColor() != null) {
			current.setColor(in.getColor());
		}

		return current.update();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#updateCalendarEvent(java.lang.String,
	 * com.google.gdata.data.calendar.CalendarEventEntry)
	 */
	public CalendarEventEntry updateCalendarEvent(String id,
			CalendarEventEntry in) throws Exception {

		CalendarEventEntry current = getCalendarEventById(id);

		if (in.getTitle() != null) {
			current.setTitle(in.getTitle());
		}

		if (in.getSummary() != null) {
			current.setSummary(in.getSummary());
		}

		return current.update();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#deleteCalendar(java.lang.String)
	 */
	public void deleteCalendar(String id) throws Exception {

		CalendarEntry current = getCalendarById(id);

		current.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#deleteCalendarEvent(java.lang.String)
	 */
	public void deleteCalendarEvent(String id) throws Exception {

		CalendarEventEntry current = getCalendarEventById(id);

		current.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#subscribeToCalendar(java.lang.String)
	 */
	public CalendarEntry subscribeToCalendar(String id) throws Exception {

		CalendarEntry calendar = new CalendarEntry();
		calendar.setId(id);
		URL feedUrl = new URL(
				"http://www.google.com/calendar/feeds/default/allcalendars/full");
		return getRealService().insert(feedUrl, calendar);

	}

	protected URL getCalendarEventUrl(CalendarEntry cal) throws Exception {
		URL feedUrl = new URL("http://www.google.com/calendar/feeds/"
				+ getCalendarId(cal) + "/private/full");
		return feedUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seegoofs.calendar.ICalendar#getEvents(com.google.gdata.data.calendar.
	 * CalendarEntry)
	 */
	public List<CalendarEventEntry> getEvents(CalendarEntry cal)
			throws Exception {

		CalendarEventFeed feed = getRealService().getFeed(
				getCalendarEventUrl(cal), CalendarEventFeed.class);

		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seegoofs.calendar.ICalendar#getEvents(com.google.gdata.data.calendar.
	 * CalendarEntry, java.util.Date, java.util.Date)
	 */
	public List<CalendarEventEntry> getEvents(CalendarEntry cal, Date start,
			Date end) throws Exception {

		CalendarQuery q = new CalendarQuery(getCalendarEventUrl(cal));
		DateTime min = new DateTime(start);
		min.setDateOnly(false);
		DateTime max = new DateTime(end);
		max.setDateOnly(false);
		q.setMinimumStartTime(min);
		q.setMaximumStartTime(max);
		CalendarEventFeed feed = getRealService().query(q,
				CalendarEventFeed.class);
		return feed.getEntries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seegoofs.calendar.ICalendar#getEvents(com.google.gdata.data.calendar.
	 * CalendarEntry, java.lang.String)
	 */
	public List<CalendarEventEntry> getEvents(CalendarEntry cal, String query)
			throws Exception {

		CalendarQuery q = new CalendarQuery(getCalendarEventUrl(cal));
		q.setFullTextQuery(query);
		CalendarEventFeed feed = getRealService().query(q,
				CalendarEventFeed.class);
		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * goofs.calendar.ICalendar#createQuickEvent(com.google.gdata.data.calendar
	 * .CalendarEntry, java.lang.String)
	 */
	public CalendarEventEntry createQuickEvent(CalendarEntry cal, String event)
			throws Exception {

		CalendarEventEntry e = new CalendarEventEntry();
		Reminder reminder = new Reminder();
		reminder.setMethod(Method.ALL);
		e.getReminder().add(reminder);
		e.setContent(new PlainTextConstruct(event));
		e.setQuickAdd(true);
		return getRealService().insert(getCalendarEventUrl(cal), e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.calendar.ICalendar#getExtensionProfile()
	 */
	public ExtensionProfile getExtensionProfile() {

		return getRealService().getExtensionProfile();
	}

}
