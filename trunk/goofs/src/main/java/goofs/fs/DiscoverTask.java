package goofs.fs;

import goofs.EntryContainer;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

public class DiscoverTask extends TimerTask {

	protected Dir root;

	public DiscoverTask(Dir root) {
		setRoot(root);
	}

	protected Dir getRoot() {
		return root;
	}

	protected void setRoot(Dir root) {
		this.root = root;
	}

	@Override
	public void run() {

		discover(getRoot());

	}

	protected void discover(Dir root) {

		try {

			for (Node next : root.getFiles().values()) {

				if (next instanceof EntryContainer) {

					EntryContainer ec = (EntryContainer) next;
					Set<String> currIds = ec.getEntryIds();
					Set<String> latestIds = ec.getCurrentEntryIds();
					Set<String> difference = new HashSet<String>(latestIds);
					difference.removeAll(currIds);
					for (String id : difference) {
						ec.addNewEntryById(id);
					}

				}

				if (next instanceof Dir) {

					discover((Dir) next);
				}

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
