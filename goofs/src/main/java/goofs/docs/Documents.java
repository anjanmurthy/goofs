package goofs.docs;

import java.io.File;
import java.net.URL;
import java.util.List;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.util.AuthenticationException;

public class Documents implements IDocuments {

	protected DocsService realService;

	public Documents(String username, String password)
			throws AuthenticationException {

		realService = new DocsService(APP_NAME);
		realService.setUserCredentials(username, password);

	}

	public DocsService getRealService() {
		return realService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getDocuments()
	 */
	public List<DocumentListEntry> getDocuments() throws Exception {

		DocumentListFeed feed = getRealService().getFeed(
				new URL("/feeds/documents/private/full?showfolders=true"),
				DocumentListFeed.class);

		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getFolders()
	 */
	public List<FolderEntry> getFolders() throws Exception {

		DocumentListFeed feed = getRealService()
				.getFeed(
						new URL(
								"/feeds/documents/private/full/-/folder?showfolders=true"),
						DocumentListFeed.class);

		return feed.getEntries(FolderEntry.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getWpDocuments()
	 */
	public List<DocumentListEntry> getWpDocuments() throws Exception {

		DocumentListFeed feed = getRealService()
				.getFeed(
						new URL(
								"/feeds/documents/private/full/-/document?showfolders=true"),
						DocumentListFeed.class);

		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getSpreadSheets()
	 */
	public List<DocumentListEntry> getSpreadSheets() throws Exception {

		DocumentListFeed feed = getRealService()
				.getFeed(
						new URL(
								"/feeds/documents/private/full/-/spreadsheet?showfolders=true"),
						DocumentListFeed.class);

		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getPresentations()
	 */
	public List<DocumentListEntry> getPresentations() throws Exception {

		DocumentListFeed feed = getRealService()
				.getFeed(
						new URL(
								"/feeds/documents/private/full/-/presentation?showfolders=true"),
						DocumentListFeed.class);

		return feed.getEntries();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getDocumentById(java.lang.String)
	 */
	public DocumentListEntry getDocumentById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), DocumentListEntry.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getFolderById(java.lang.String)
	 */
	public FolderEntry getFolderById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), FolderEntry.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#deleteDocument(java.lang.String)
	 */
	public void deleteDocument(String id) throws Exception {

		DocumentListEntry doc = getDocumentById(id);

		doc.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#removeDocumentFromFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void removeDocumentFromFolder(String docId, String folderId)
			throws Exception {

		URL deleteUrl = new URL("/feeds/folders/private/full/folder%3A"
				+ folderId + "/document%3A" + docId);

		getRealService().delete(deleteUrl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#addDocumentToFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void addDocumentToFolder(String docId, String folderId)
			throws Exception {

		URL postUrl = new URL("/feeds/folders/private/full/folder%3A"
				+ folderId);

		DocumentListEntry doc = getDocumentById(docId);

		getRealService().insert(postUrl, doc);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#moveFolderToFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void moveFolderToFolder(String fromFolderId, String toFolderId)
			throws Exception {

		URL postUrl = new URL("/feeds/folders/private/full/folder%3A"
				+ toFolderId);

		FolderEntry folder = getFolderById(fromFolderId);

		getRealService().insert(postUrl, folder);

	}

	protected <T extends DocumentListEntry> T createDocument(String name,
			File contents, String folderName, T newDocument) throws Exception {

		newDocument.setFile(contents, MediaType.fromFileName(name)
				.getMimeType());
		newDocument.setTitle(new PlainTextConstruct(name.split("\\.")[0]));

		if (folderName != null) {

			Person owner = new Person();
			owner.setEmail(System.getProperty("username"));
			newDocument.addFolder(owner, folderName);

		}

		return getRealService().insert(
				new URL("/feeds/documents/private/full"), newDocument);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#createWPDocument(java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public DocumentEntry createWPDocument(String name, File contents,
			String folderName) throws Exception {

		DocumentEntry newDocument = new DocumentEntry();

		return createDocument(name, contents, folderName, newDocument);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#createSpreadsheet(java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public SpreadsheetEntry createSpreadsheet(String name, File contents,
			String folderName) throws Exception {

		SpreadsheetEntry newDocument = new SpreadsheetEntry();

		return createDocument(name, contents, folderName, newDocument);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#createPresentation(java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public PresentationEntry createPresentation(String name, File contents,
			String folderName) throws Exception {

		PresentationEntry newDocument = new PresentationEntry();

		return createDocument(name, contents, folderName, newDocument);

	}
}
