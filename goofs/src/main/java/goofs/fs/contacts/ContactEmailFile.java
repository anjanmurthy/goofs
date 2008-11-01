package goofs.fs.contacts;

import java.util.List;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.File;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class ContactEmailFile extends File {

	public ContactEmailFile(Dir parent, Email email) throws Exception {

		super(parent, email.getPrimary() ? "primary" : email.getRel()
				.split("#")[1], 0755, email.getAddress());

	}

	public ContactEntry getContact() {
		return ((ContactEmailDir) getParent()).getContact();
	}

	public void setContact(ContactEntry contact) {
		((ContactEmailDir) getParent()).setContact(contact);
	}

	protected Contacts getContacts() {

		return ((ContactEmailDir) getParent()).getContacts();
	}

	@Override
	public int save() {
		try {
			List<Email> emails = getContact().getEmailAddresses();

			for (Email email : emails) {

				if (email.getRel().equals(getName())) {

					email.setAddress(new String(getContent()));

				}

			}

			setContact(getContacts().updateContact(getContact()));

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
		return Errno.EROFS;
	}

}
