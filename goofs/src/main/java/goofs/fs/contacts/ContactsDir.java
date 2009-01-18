package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

import com.google.gdata.data.contacts.ContactEntry;

public class ContactsDir extends Dir {

	protected Contacts contacts;

	public ContactsDir(Dir parent) throws Exception {

		super(parent, resourceBundle.getString("goofs.contacts.contacts"), 0755);

		contacts = new Contacts(System.getProperty("username"), System
				.getProperty("password"));

		List<ContactEntry> entries = contacts.getContacts();

		for (ContactEntry entry : entries) {

			ContactDir contactDir = new ContactDir(this, entry);

			add(contactDir);
		}

	}

	public Contacts getContacts() {
		return contacts;
	}

	@Override
	public int createChild(String name, boolean isDir) {
		// TODO Auto-generated method stub
		return 0;
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
		return Errno.EROFS;
	}

}
