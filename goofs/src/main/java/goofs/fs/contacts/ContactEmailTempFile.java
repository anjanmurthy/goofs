package goofs.fs.contacts;

import fuse.Errno;
import goofs.contacts.Contacts;
import goofs.fs.Dir;
import goofs.fs.SimpleFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class ContactEmailTempFile extends SimpleFile {

	private static Set<String> RELS = new HashSet<String>();

	static {
		RELS.add("primary");
		RELS.add(Email.Rel.WORK.split("#")[1]);
		RELS.add(Email.Rel.HOME.split("#")[1]);
		RELS.add(Email.Rel.OTHER.split("#")[1]);
	}

	public ContactEmailTempFile(Dir parent, String name) throws Exception {
		super(parent, name);

	}

	@Override
	public int rename(Dir newParent, String name) {

		int rt = super.rename(newParent, name);

		if (rt == 0) {

			if (RELS.contains(getName())) {

				Contacts contacts = ((ContactEmailDir) getParent())
						.getContacts();
				ContactEntry contact = ((ContactEmailDir) getParent())
						.getContact();

				List<Email> emails = contact.getEmailAddresses();

				Email e = findMatch(emails);

				if (e != null) {
					e.setAddress(new String(getContent()));

					try {
						contacts.updateContact(contact);

						remove();

						getParent().add(new ContactEmailFile(getParent(), e));

						return 0;

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();

						return Errno.EROFS;
					}
				}
			}

		}

		return rt;

	}

	protected Email findMatch(List<Email> emails) {
		for (Email e : emails) {

			if ("primary".equals(getName()) && e.getPrimary()) {
				return e;
			}

			else if (e.getRel().equals(getName())) {
				return e;
			}

		}
		return null;

	}
}