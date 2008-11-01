package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.Node;

import java.util.List;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class ContactEmailDir extends Dir {

	public ContactEmailDir(Dir parent) throws Exception {

		super(parent, "email", 0755);

		List<Email> emails = getContact().getEmailAddresses();

		for (Email email : emails) {

			add(new ContactEmailFile(this, email));

		}

	}

	public ContactEntry getContact() {
		return ((ContactDir) getParent()).getContact();
	}

	public void setContact(ContactEntry contact) {
		((ContactDir) getParent()).setContact(contact);
	}

	protected Contacts getContacts() {

		return ((ContactDir) getParent()).getContacts();
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
		return Errno.EROFS;
	}

}
