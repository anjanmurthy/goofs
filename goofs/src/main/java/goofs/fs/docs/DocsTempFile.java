package goofs.fs.docs;

import fuse.Errno;
import goofs.docs.IDocuments;
import goofs.fs.Dir;
import goofs.fs.DiskFile;

import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;

public class DocsTempFile extends DiskFile {

	public DocsTempFile(Dir parent, String name) throws Exception {

		super(parent, name, 0755);
		// TODO Auto-generated constructor stub
	}

	public int delete() {

		remove();

		return 0;
	}

	@Override
	public int save() {
		return 0;
	}

	@Override
	public int rename(Dir newParent, String name) {

		if (getParent() == newParent) {

			setName(name);

			try {
				if (getDocuments().isWPDocument(getName())) {

					DocumentEntry doc = getDocuments().createWPDocument(
							getName(), getDisk(), null);

					remove();

					getParent().add(new DocsFile(getParent(), doc));

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								doc.getId());

					}
				}

				else if (getDocuments().isSpreadSheet(getName())) {

					SpreadsheetEntry sp = getDocuments().createSpreadsheet(
							getName(), getDisk(), null);

					remove();

					getParent().add(new DocsFile(getParent(), sp));

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								sp.getId());

					}

				}

				else if (getDocuments().isPresentation(getName())) {

					PresentationEntry pr = getDocuments().createPresentation(
							getName(), getDisk(), null);

					remove();

					getParent().add(new DocsFile(getParent(), pr));

					Dir parent = getParent();
					if (parent instanceof DocsFolderDir) {

						getDocuments().addDocumentToFolder(
								((DocsFolderDir) parent).getFolderId(),
								pr.getId());

					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				return Errno.EROFS;
			}

		}

		return 0;
	}

	protected IDocuments getDocuments() {
		Dir parent = getParent();
		if (parent instanceof DocsFolderDir) {
			return ((DocsFolderDir) parent).getDocuments();
		} else {
			return ((DocsDir) parent).getDocuments();
		}

	}

}
