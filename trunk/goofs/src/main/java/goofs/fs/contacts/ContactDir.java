package goofs.fs.contacts;

import fuse.Errno;
import goofs.Fetchable;
import goofs.Identifiable;
import goofs.NotFoundException;
import goofs.contacts.IContacts;
import goofs.fs.Dir;
import goofs.fs.Node;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;

public class ContactDir extends Dir implements Identifiable, Fetchable {

	protected String contactId;

	public ContactDir(Dir parent, ContactEntry contact) {

		super(parent, contact.getTitle().getPlainText().length() == 0 ? contact
				.getEmailAddresses().get(0).getAddress() : contact.getTitle()
				.getPlainText(), 0755);

		setContactId(contact.getId());

		try {
			if (getContacts().hasPhotoContent(contact)) {

				try {
					ContactPhotoFile photoFile = new ContactPhotoFile(this,
							getName() + ".jpg", getContacts()
									.getContactPhotoInputStream(contact));

					add(photoFile);

				} catch (Exception e) {
				}

			}

			add(new ContactEmailDir(this));

			add(new ContactNotesFile(this, contact));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getId() {
		return getContactId();
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public ContactEntry getContact() {

		try {
			return getContacts().getContactById(getContactId());
		} catch (Exception e) {

			e.printStackTrace();

			return null;
		}

	}

	public Object fetch() throws NotFoundException {

		Object o = getContact();
		if (o == null) {
			throw new NotFoundException(toString());
		}
		return o;
	}

	protected IContacts getContacts() {

		return ((ContactsDir) getParent()).getContacts();
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
		try {
			ContactNotesTempFile f = new ContactNotesTempFile(this, name);

			add(f);

			return 0;

		} catch (Exception e) {
			return Errno.EROFS;
		}
	}

	@Override
	public int delete() {

		try {
			getContacts().deleteContact(getContact());

			remove();

			return 0;
		} catch (Exception e) {
			return Errno.EROFS;
		}

	}

	@Override
	public int rename(Dir newParent, String name) {

		if (getParent() == newParent) {

			try {

				ContactEntry current = getContact();

				current.setTitle(new PlainTextConstruct(name));

				getContacts().updateContact(current);

				setName(name);

				return 0;
			} catch (Exception e) {

				e.printStackTrace();

			}

		}

		return Errno.EROFS;
	}

}