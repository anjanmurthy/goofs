package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.Node;
import goofs.fs.SimpleFile;

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

	protected Contacts getContacts() {

		return ((ContactDir) getParent()).getContacts();
	}

	@Override
	public int createChild(String name, boolean isDir) {

		if (!isDir) {
			try {
				Email email = new Email();
				if ("work".equals(name)) {
					email.setRel(Email.Rel.WORK);

				} else if ("home".equals(name)) {
					email.setRel(Email.Rel.HOME);
				} else {
					email.setRel(Email.Rel.OTHER);
					email.setLabel(name);
				}

				add(new ContactEmailFile(this, email));

				return 0;

			} catch (Exception e) {

				e.printStackTrace();
			}

		}

		return Errno.EROFS;
	}

	@Override
	public int createChildFromExisting(String name, Node child) {
		return Errno.EROFS;
	}

	@Override
	public int createTempChild(String name) {
		try {
			SimpleFile f = new SimpleFile(this, name);

			add(f);

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