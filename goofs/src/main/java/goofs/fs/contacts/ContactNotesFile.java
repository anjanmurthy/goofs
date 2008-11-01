package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.File;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;

public class ContactNotesFile extends File {

	public ContactNotesFile(Dir parent, ContactEntry contact) throws Exception {

		super(parent, "notes", 0755, (contact.getContent() == null) ? ""
				: contact.getTextContent().getContent().getPlainText());

	}

	protected Contacts getContacts() {

		return ((ContactsDir) getParent().getParent()).getContacts();
	}

	public ContactEntry getContact() {
		return ((ContactDir) getParent()).getContact();
	}

	public void setContact(ContactEntry contact) {
		((ContactDir) getParent()).setContact(contact);
	}

	@Override
	public int save() {

		getContact().setContent(
				new PlainTextConstruct(new String(getContent())));

		try {
			setContact(getContacts().updateContact(getContact()));

			return 0;
		} catch (Exception e) {
			return Errno.EROFS;
		}

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
