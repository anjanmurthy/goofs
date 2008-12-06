package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.File;

import java.util.ArrayList;
import java.util.List;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class ContactEmailFile extends File {

	private Email email;

	public ContactEmailFile(Dir parent, Email email) throws Exception {

		super(parent, email.getPrimary() ? "primary" : email.getRel()
				.split("#")[1], 0755, email.getAddress() != null ? email
				.getAddress() : "");

		this.email = email;
	}

	public ContactEntry getContact() {
		return ((ContactEmailDir) getParent()).getContact();
	}

	protected Contacts getContacts() {

		return ((ContactEmailDir) getParent()).getContacts();
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	@Override
	public int save() {
		try {

			ContactEntry contact = getContact();

			if (getEmail().getAddress() == null) {
				contact.getEmailAddresses().add(getEmail());
			}

			getEmail().setAddress(new String(getContent()));

			getContacts().updateContact(contact);

			return 0;
		} catch (Exception e) {

			e.printStackTrace();

			return Errno.EROFS;
		}

	}

	@Override
	public int delete() {
		try {

			ContactEntry contact = getContact();

			if (getEmail().getAddress() != null) {

				List<Email> emails = contact.getEmailAddresses();

				List<Email> newEmails = new ArrayList<Email>();

				for (Email e : emails) {

					if (!getEmail().getAddress().equals(e.getAddress())) {

						newEmails.add(email);

					}

				}

				contact.getEmailAddresses().clear();
				contact.getEmailAddresses().addAll(newEmails);

				getContacts().updateContact(contact);
			}

			remove();

			return 0;
		} catch (Exception e) {

			e.printStackTrace();
		}

		return Errno.EROFS;
	}

	@Override
	public int rename(Dir newParent, String name) {
		return Errno.EROFS;
	}

}
