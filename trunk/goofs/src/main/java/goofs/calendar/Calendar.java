package goofs.calendar;

import goofs.GoofsService;

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

public class Calendar implements GoofsService {

	protected CalendarService realService;

	public Calendar(String userName, String password)
			throws AuthenticationException {

		realService = new CalendarService(APP_NAME);
		realService.setUserCredentials(userName, password);
	}

	protected CalendarService getRealService() {
		return realService;
	}

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

	public CalendarEntry createCalendar(String title) throws Exception {

		CalendarEntry calendar = new CalendarEntry();
		calendar.setTitle(new PlainTextConstruct(title));
		URL postUrl = new URL(
				"http://www.google.com/calendar/feeds/default/owncalendars/full");
		return getRealService().insert(postUrl, calendar);

	}

	public CalendarEntry getCalendarById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), CalendarEntry.class);

	}

	public CalendarEventEntry getCalendarEventById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), CalendarEventEntry.class);
	}

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

	public void deleteCalendar(String id) throws Exception {

		CalendarEntry current = getCalendarById(id);

		current.delete();
	}

	public void deleteCalendarEvent(String id) throws Exception {

		CalendarEventEntry current = getCalendarEventById(id);

		current.delete();
	}

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

	public List<CalendarEventEntry> getEvents(CalendarEntry cal)
			throws Exception {

		CalendarEventFeed feed = getRealService().getFeed(
				getCalendarEventUrl(cal), CalendarEventFeed.class);

		return feed.getEntries();

	}

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

	public List<CalendarEventEntry> getEvents(CalendarEntry cal, String query)
			throws Exception {

		CalendarQuery q = new CalendarQuery(getCalendarEventUrl(cal));
		q.setFullTextQuery(query);
		CalendarEventFeed feed = getRealService().query(q,
				CalendarEventFeed.class);
		return feed.getEntries();

	}

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

	public ExtensionProfile getExtensionProfile() {

		return getRealService().getExtensionProfile();
	}

}
