package goofs;

import java.lang.reflect.Proxy;

import goofs.blogger.Blogger;
import goofs.blogger.IBlogger;
import goofs.calendar.Calendar;
import goofs.calendar.ICalendar;
import goofs.contacts.Contacts;
import goofs.contacts.IContacts;
import goofs.photos.IPicasa;
import goofs.photos.Picasa;

public class ServiceFactory {

	public static Object getService(Class clazz) throws Exception {

		GoofsService gs = null;

		if (clazz == IBlogger.class) {
			gs = new Blogger(System.getProperty("username"), System
					.getProperty("password"));
		} else if (clazz == ICalendar.class) {
			gs = new Calendar(System.getProperty("username"), System
					.getProperty("password"));
		} else if (clazz == IContacts.class) {
			gs = new Contacts(System.getProperty("username"), System
					.getProperty("password"));
		} else if (clazz == IPicasa.class) {
			gs = new Picasa(System.getProperty("username"), System
					.getProperty("password"));
		} else {
			throw new IllegalArgumentException(clazz.getName());
		}

		return Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new ServiceInvocationHandler(gs));

	}
}
