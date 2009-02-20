package goofs.docs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.Link;
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

	protected GoogleService spreadsheetsService;

	protected static final String SPREADSHEETS_SERVICE_NAME = "wise";

	private static final Map<String, String> DOWNLOAD_DOCUMENT_FORMATS;
	static {
		DOWNLOAD_DOCUMENT_FORMATS = new HashMap<String, String>();
		DOWNLOAD_DOCUMENT_FORMATS.put("doc", "doc");
		DOWNLOAD_DOCUMENT_FORMATS.put("txt", "txt");
		DOWNLOAD_DOCUMENT_FORMATS.put("odt", "odt");
		DOWNLOAD_DOCUMENT_FORMATS.put("pdf", "pdf");
		DOWNLOAD_DOCUMENT_FORMATS.put("png", "png");
		DOWNLOAD_DOCUMENT_FORMATS.put("rtf", "rtf");
		DOWNLOAD_DOCUMENT_FORMATS.put("html", "html");
	}

	private static final Map<String, String> DOWNLOAD_PRESENTATION_FORMATS;
	static {
		DOWNLOAD_PRESENTATION_FORMATS = new HashMap<String, String>();
		DOWNLOAD_PRESENTATION_FORMATS.put("pdf", "pdf");
		DOWNLOAD_PRESENTATION_FORMATS.put("ppt", "ppt");
		DOWNLOAD_PRESENTATION_FORMATS.put("swf", "swf");
	}

	private static final Map<String, String> DOWNLOAD_SPREADSHEET_FORMATS;
	static {
		DOWNLOAD_SPREADSHEET_FORMATS = new HashMap<String, String>();
		DOWNLOAD_SPREADSHEET_FORMATS.put("xls", "4");
		DOWNLOAD_SPREADSHEET_FORMATS.put("ods", "13");
		DOWNLOAD_SPREADSHEET_FORMATS.put("pdf", "12");
		DOWNLOAD_SPREADSHEET_FORMATS.put("csv", "5");
		DOWNLOAD_SPREADSHEET_FORMATS.put("tsv", "23");
		DOWNLOAD_SPREADSHEET_FORMATS.put("html", "102");
	}

	public Documents(String username, String password)
			throws AuthenticationException {

		realService = new DocsService(APP_NAME);
		realService.setUserCredentials(username, password);
		spreadsheetsService = new GoogleService(SPREADSHEETS_SERVICE_NAME,
				APP_NAME);
		spreadsheetsService.setUserCredentials(username, password);
	}

	public DocsService getRealService() {
		return realService;
	}

	public GoogleService getSpreadsheetsService() {
		return spreadsheetsService;
	}

	public void acquireSessionTokens(String username, String password)
			throws AuthenticationException {

		getRealService().setUserCredentials(username, password);

		getSpreadsheetsService().setUserCredentials(username, password);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getDocuments()
	 */
	public List<DocumentListEntry> getDocuments() throws Exception {

		DocumentListFeed feed = getRealService().getFeed(
				new URL("http://docs.google.com/feeds/documents/private/full"),
				DocumentListFeed.class);

		return feed.getEntries();

	}

	public List<DocumentListEntry> getDocumentsInFolder(String folderId)
			throws Exception {

		String folderIdShort = folderId.split("%3A")[1];

		DocumentListFeed feed = getRealService().getFeed(
				new URL(
						"http://docs.google.com/feeds/folders/private/full/folder%3A"
								+ folderIdShort), DocumentListFeed.class);

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
								"http://docs.google.com/feeds/documents/private/full/-/folder?showfolders=true"),
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
								"http://docs.google.com/feeds/documents/private/full/-/document?showfolders=true"),
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
								"http://docs.google.com/feeds/documents/private/full/-/spreadsheet?showfolders=true"),
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
								"http://docs.google.com/feeds/documents/private/full/-/presentation?showfolders=true"),
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

	protected String getDocumentIdSuffix(String objectId) {

		return objectId.substring(objectId.lastIndexOf("%3A") + 3);
	}

	public InputStream getDocumentContents(DocumentListEntry e, String mt)
			throws Exception {

		Link link = new Link();
		if ("odt".equals(mt)) {
			link
					.setHref("http://docs.google.com/feeds/download/documents/Export?docID="
							+ getDocumentIdSuffix(e.getId())
							+ "&exportFormat="
							+ DOWNLOAD_DOCUMENT_FORMATS.get(mt));

			return getRealService().getStreamFromLink(link);

		} else if ("ods".equals(mt)) {
			link
					.setHref("http://spreadsheets.google.com/feeds/download/spreadsheets/Export?key="
							+ getDocumentIdSuffix(e.getId())
							+ "&fmcmd="
							+ DOWNLOAD_SPREADSHEET_FORMATS.get(mt) + "&gid=1");

			return getSpreadsheetsService().getStreamFromLink(link);

		} else if ("ppt".equals(mt)) {
			link
					.setHref("http://docs.google.com/feeds/download/presentations/Export?docID="
							+ getDocumentIdSuffix(e.getId())
							+ "&exportFormat="
							+ DOWNLOAD_PRESENTATION_FORMATS.get(mt));

			return getRealService().getStreamFromLink(link);
		}

		return new ByteArrayInputStream("".getBytes());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#getFolderById(java.lang.String)
	 */
	public FolderEntry getFolderById(String id) throws Exception {

		return getRealService().getEntry(new URL(id), FolderEntry.class);
	}

	public List<Link> getFolderParentLinks(String folderId) throws Exception {

		return getFolderById(folderId).getParentLinks();

	}

	public List<DocumentListEntry> getRootDocuments() throws Exception {

		List<DocumentListEntry> roots = new ArrayList<DocumentListEntry>();
		for (DocumentListEntry next : getDocuments()) {

			if (next.getFolders().isEmpty()) {
				roots.add(next);
			}
		}
		return roots;
	}

	public List<FolderEntry> getRootFolders() throws Exception {

		List<FolderEntry> roots = new ArrayList<FolderEntry>();
		for (FolderEntry next : getFolders()) {

			if (next.getParentLinks().isEmpty()) {
				roots.add(next);
			}
		}

		return roots;

	}

	public List<FolderEntry> getChildFolders(String parentFolderId)
			throws Exception {

		return getChildFolders(getFolderById(parentFolderId));

	}

	public List<FolderEntry> getChildFolders(FolderEntry parent)
			throws Exception {

		List<FolderEntry> childs = new ArrayList<FolderEntry>();
		for (FolderEntry next : getFolders()) {
			for (Link link : next.getParentLinks()) {
				if (link.getHref().equals(parent.getSelfLink().getHref())) {
					childs.add(next);
				}
			}
		}
		return childs;

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

	public void deleteFolder(String id) throws Exception {

		FolderEntry folder = getFolderById(id);

		folder.delete();
	}

	public void renameDocument(String id, String name) throws Exception {

		DocumentListEntry doc = getDocumentById(id);
		int dindex = name.lastIndexOf(".");
		if (dindex != -1) {

			doc.setTitle(new PlainTextConstruct(name.substring(dindex + 1)));

		} else {
			doc.setTitle(new PlainTextConstruct(name));
		}

		doc.update();

	}

	public void renameFolder(String id, String name) throws Exception {

		FolderEntry folder = getFolderById(id);

		folder.setTitle(new PlainTextConstruct(name));

		folder.update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#removeDocumentFromFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void removeDocumentFromFolder(String folderId, String docId)
			throws Exception {

		String folderIdShort = folderId.split("%3A")[1];

		String docIdShort = docId.split("%3A")[1];

		URL deleteUrl = new URL(
				"http://docs.google.com/feeds/folders/private/full/folder%3A"
						+ folderIdShort + "/document%3A" + docIdShort);

		getRealService().delete(deleteUrl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#addDocumentToFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void addDocumentToFolder(String folderId, String docId)
			throws Exception {

		DocumentListEntry doc = getDocumentById(docId);

		addDocumentToFolder(folderId, doc);
	}

	protected void addDocumentToFolder(String folderId, DocumentListEntry doc)
			throws Exception {

		String urlBase = "http://docs.google.com/feeds/folders/private/full/folder%3A";

		String shortId = folderId.split("%3A")[1];

		URL postUrl = new URL(urlBase + shortId);

		getRealService().insert(postUrl, doc);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see goofs.docs.IDocuments#moveFolderToFolder(java.lang.String,
	 * java.lang.String)
	 */
	public void moveFolderToFolder(String toFolderId, String fromFolderId)
			throws Exception {

		String urlBase = "http://docs.google.com/feeds/folders/private/full/folder%3A";

		String shortId = toFolderId.split("%3A")[1];

		URL postUrl = new URL(urlBase + shortId);

		FolderEntry folder = getFolderById(fromFolderId);

		getRealService().insert(postUrl, folder);

	}

	public FolderEntry creatFolder(String name) throws Exception {

		FolderEntry folder = new FolderEntry();
		folder.setTitle(new PlainTextConstruct(name));

		return getRealService().insert(
				new URL("http://docs.google.com/feeds/documents/private/full"),
				folder);

	}

	protected <T extends DocumentListEntry> T createDocument(String name,
			File contents, String folderId, T newDocument) throws Exception {

		newDocument.setFile(contents, MediaType.fromFileName(name)
				.getMimeType());

		int dindex = name.lastIndexOf(".");
		if (dindex != -1) {

			newDocument.setTitle(new PlainTextConstruct(name
					.substring(dindex + 1)));

		} else {

			newDocument.setTitle(new PlainTextConstruct(name));
		}

		T created = getRealService().insert(
				new URL("http://docs.google.com/feeds/documents/private/full"),
				newDocument);

		if (folderId != null) {
			addDocumentToFolder(folderId, created);
		}

		return created;

	}

	public boolean isWPDocument(String fileName) {

		MediaType m = MediaType.fromFileName(fileName);

		return (m != null && (m.equals(MediaType.DOC)
				|| m.equals(MediaType.RTF) || m.equals(MediaType.ODT)
				|| m.equals(MediaType.TXT) || m.equals(MediaType.HTM) || m
				.equals(MediaType.HTML)));
	}

	public boolean isSpreadSheet(String fileName) {

		MediaType m = MediaType.fromFileName(fileName);

		return (m != null && (m.equals(MediaType.XLS)
				|| m.equals(MediaType.CSV) || m.equals(MediaType.ODS)
				|| m.equals(MediaType.TAB) || m.equals(MediaType.TSV)));

	}

	public boolean isPresentation(String fileName) {

		MediaType m = MediaType.fromFileName(fileName);

		return (m != null && (m.equals(MediaType.PPT) || m
				.equals(MediaType.PPS)));

	}

	public void updateDocumentContent(String id, File contents)
			throws Exception {

		DocumentListEntry doc = getDocumentById(id);

		doc.setFile(contents, MediaType.fromFileName(
				doc.getTitle().getPlainText()).getMimeType());

		doc.updateMedia(false);

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
