package goofs.fs.calendar;

import fuse.Errno;
import goofs.EntryContainer;
import goofs.calendar.ICalendar;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;

public class CalendarEventDurationDir extends Dir implements EntryContainer {

	protected Date start;

	protected Date end;

	protected Set<String> entryIds = new HashSet<String>();

	public CalendarEventDurationDir(Dir parent, String name, CalendarEntry cal,
			Date start, Date end) throws Exception {

		super(parent, name, 0755);

		setStart(start);

		setEnd(end);

		List<CalendarEventEntry> events = getCalendarService().getEvents(cal,
				start, end);

		for (CalendarEventEntry event : events) {

			CalendarEventDir eventDir = new CalendarEventDir(this, event);

			add(eventDir);

			getEntryIds().add(event.getSelfLink().getHref());

		}

	}

	public Set<String> getEntryIds() {
		return entryIds;
	}

	public void addNewEntryById(String entryId) throws Exception {

		CalendarEventEntry event = getCalendarService().getCalendarEventById(
				entryId);

		CalendarEventDir eventDir = new CalendarEventDir(this, event);

		add(eventDir);

		getEntryIds().add(event.getSelfLink().getHref());

	}

	public Set<String> getCurrentEntryIds() throws Exception {
		Set<String> ids = new HashSet<String>();

		List<CalendarEventEntry> events = getCalendarService().getEvents(
				getCalendar(), start, end);

		for (CalendarEventEntry event : events) {
			ids.add(event.getSelfLink().getHref());
		}

		return ids;
	}

	protected ICalendar getCalendarService() {

		return ((CalendarsDir) getParent().getParent()).getCalendarService();
	}

	protected CalendarEntry getCalendar() {

		return ((CalendarDir) getParent()).getCalendar();

	}

	protected Date getStart() {
		return start;
	}

	protected void setStart(Date start) {
		this.start = start;
	}

	protected Date getEnd() {
		return end;
	}

	protected void setEnd(Date end) {
		this.end = end;
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

		if (getParent() == newParent) {

			((CalendarDir) parent).createChild(name, true);

			remove();

		}

		return 0;
	}

}
