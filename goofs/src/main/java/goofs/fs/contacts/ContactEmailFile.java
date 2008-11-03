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

	public void setContact(ContactEntry contact) {
		((ContactEmailDir) getParent()).setContact(contact);
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

			if (getEmail().getAddress() == null) {
				getContact().getEmailAddresses().add(getEmail());
			}

			getEmail().setAddress(new String(getContent()));

			setContact(getContacts().updateContact(getContact()));

			return 0;
		} catch (Exception e) {

			e.printStackTrace();

			return Errno.EROFS;
		}

	}

	@Override
	public int delete() {
		try {

			if (getEmail().getAddress() != null) {

				List<Email> emails = getContact().getEmailAddresses();

				List<Email> newEmails = new ArrayList<Email>();

				for (Email e : emails) {

					if (!getEmail().getAddress().equals(e.getAddress())) {

						newEmails.add(email);

					}

				}

				getContact().getEmailAddresses().clear();
				getContact().getEmailAddresses().addAll(newEmails);

				setContact(getContacts().updateContact(getContact()));
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
