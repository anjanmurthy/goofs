package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.SimpleFile;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;

public class ContactNotesTempFile extends SimpleFile {

	public ContactNotesTempFile(Dir parent, String name) throws Exception {
		super(parent, name);

	}

	@Override
	public int rename(Dir newParent, String name) {

		int rt = super.rename(newParent, name);

		if (rt == 0) {

			if ("notes".equals(getName())) {

				Contacts contacts = ((ContactsDir) getParent().getParent())
						.getContacts();

				ContactEntry contact = ((ContactDir) getParent()).getContact();

				contact.setContent(new PlainTextConstruct(new String(
						getContent())));

				try {
					contacts.updateContact(contact);

					remove();

					getParent().add(new ContactNotesFile(getParent(), contact));

					return 0;

				} catch (Exception e) {

					e.printStackTrace();

					return Errno.EROFS;
				}
			}

		}

		return rt;

	}

}
