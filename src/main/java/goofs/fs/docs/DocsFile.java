package goofs.fs.docs;

import fuse.Errno;
import goofs.Identifiable;
import goofs.docs.IDocuments;
import goofs.fs.Dir;
import goofs.fs.DiskFile;

import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;

public class DocsFile extends DiskFile implements Identifiable {

	protected String docId;

	public DocsFile(Dir parent, DocumentListEntry doc) throws Exception {

		super(parent, doc.getTitle().getPlainText(), 0755);

		String ext = getDocuments().getDefaultExtension(doc);

		setName(getName() + "." + ext);

		try {

			setContent(getDocuments().getDocumentContents(doc, ext));

		} catch (Exception e) {

			e.printStackTrace();
		}

		setDocId(doc.getId());

	}

	public DocsFile(Dir parent, String name) throws Exception {

		super(parent, name, 0755);

		setDocId(null);

	}

	protected IDocuments getDocuments() {
		Dir parent = getParent();
		if (parent instanceof DocsFolderDir) {
			return ((DocsFolderDir) parent).getDocuments();
		} else {
			return ((DocsDir) parent).getDocuments();
		}

	}

	public String getId() {
		return getDocId();
	}

	protected String getDocId() {
		return docId;
	}

	protected void setDocId(String docId) {
		this.docId = docId;
	}

	@Override
	public int save() {

		try {
			if (getDocId() == null) {

				if (getDocuments().isWPDocument(getName())) {

					DocumentEntry doc = getDocuments().createWPDocument(
							getName(), getDisk(), null);

					setDocId(doc.getId());

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								getDocId());

					}
				}

				else if (getDocuments().isSpreadSheet(getName())) {

					SpreadsheetEntry sp = getDocuments().createSpreadsheet(
							getName(), getDisk(), null);

					setDocId(sp.getId());

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								getDocId());

					}

				}

				else if (getDocuments().isPresentation(getName())) {

					PresentationEntry pr = getDocuments().createPresentation(
							getName(), getDisk(), null);

					setDocId(pr.getId());

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								getDocId());

					}

				}

				else {

					return Errno.EROFS;

				}

			}

			else {

				getDocuments().updateDocumentContent(getDocId(), getName(),
						getDisk());

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return Errno.EROFS;
		}

		return 0;
	}

	@Override
	public int delete() {

		try {
			Dir parent = getParent();
			if (parent instanceof DocsFolderDir) {

				// sub-level so just remove association with the folder

				getDocuments().removeDocumentFromFolder(
						((DocsFolderDir) parent).getFolderId(), getDocId());

			}

			else {
				// top level so just delete it

				getDocuments().deleteDocument(getDocId());
			}

			remove();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return Errno.EROFS;
		}

		return 0;
	}

	@Override
	public int rename(Dir newParent, String name) {

		try {
			if (getParent() == newParent) {

				getDocuments().renameDocument(getDocId(), name);

				setName(name);

				if (getName().lastIndexOf('.') != -1) {

					String ext = getName().substring(
							getName().lastIndexOf('.') + 1);

					try {
						setContent(getDocuments().getDocumentContents(
								getDocId(), ext));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

			else if (newParent instanceof DocsFolderDir) {

				getDocuments().renameDocument(getDocId(), name);

				getDocuments().addDocumentToFolder(
						((DocsFolderDir) newParent).getFolderId(), getDocId());

				((DocsFolderDir) newParent).add(new DocsFile(newParent,
						getDocuments().getDocumentById(getDocId())));

				remove();

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return Errno.EROFS;
		}
		return 0;
	}

}
