package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.IContacts;
import goofs.fs.Dir;
import goofs.fs.File;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;

public class ContactNotesFile extends File {

	public ContactNotesFile(Dir parent, ContactEntry contact) throws Exception {

		super(parent, resourceBundle.getString("goofs.contacts.notes"), 0755,
				(contact.getContent() == null) ? "" : contact.getTextContent()
						.getContent().getPlainText());

	}

	protected IContacts getContacts() {

		return ((ContactsDir) getParent().getParent()).getContacts();
	}

	public ContactEntry getContact() {
		return ((ContactDir) getParent()).getContact();
	}

	@Override
	public int save() {

		ContactEntry contact = getContact();

		contact.setContent(new PlainTextConstruct(new String(getContent())));

		try {

			getContacts().updateContact(contact);

			return 0;

		} catch (Exception e) {

			e.printStackTrace();

			return Errno.EROFS;
		}

	}

	@Override
	public int delete() {
		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {

		if (getParent() == newParent) {

			setName(name);
		}

		return 0;

	}

}
