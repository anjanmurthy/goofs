package goofs.docs;

import goofs.GoofsService;

import java.io.File;
import java.util.List;

import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;

public interface IDocuments extends GoofsService {

	public abstract List<DocumentListEntry> getDocuments() throws Exception;

	public abstract List<FolderEntry> getFolders() throws Exception;

	public abstract List<DocumentListEntry> getWpDocuments() throws Exception;

	public abstract List<DocumentListEntry> getSpreadSheets() throws Exception;

	public abstract List<DocumentListEntry> getPresentations() throws Exception;

	public abstract DocumentListEntry getDocumentById(String id)
			throws Exception;

	public abstract FolderEntry getFolderById(String id) throws Exception;

	public abstract void deleteDocument(String id) throws Exception;

	public abstract void removeDocumentFromFolder(String docId, String folderId)
			throws Exception;

	public abstract void addDocumentToFolder(String docId, String folderId)
			throws Exception;

	public abstract void moveFolderToFolder(String fromFolderId,
			String toFolderId) throws Exception;

	public abstract DocumentEntry createWPDocument(String name, File contents,
			String folderName) throws Exception;

	public abstract SpreadsheetEntry createSpreadsheet(String name,
			File contents, String folderName) throws Exception;

	public abstract PresentationEntry createPresentation(String name,
			File contents, String folderName) throws Exception;

}