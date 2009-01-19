package goofs;

import java.util.Set;

public interface EntryContainer {

	// what it's holding
	public Set<String> getEntryIds();

	// fresh from the back end
	public Set<String> getCurrentEntryIds() throws Exception;

	public void addNewEntryById(String entryId) throws Exception;

}
