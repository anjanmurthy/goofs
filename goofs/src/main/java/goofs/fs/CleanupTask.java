package goofs.fs;

import goofs.Fetchable;
import goofs.NotFoundException;

import java.util.Iterator;
import java.util.TimerTask;

public class CleanupTask extends TimerTask {

	protected Dir root;

	public CleanupTask(Dir root) {
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

		prune(getRoot());

	}

	protected void prune(Dir root) {

		try {

			Iterator<Node> it = root.getFiles().values().iterator();

			while (it.hasNext()) {

				Node next = it.next();

				if (next instanceof Fetchable) {

					try {

						((Fetchable) next).fetch();
					}

					catch (NotFoundException nf) {

						next.remove();
						continue;

					}

				}

				if (next instanceof Dir) {

					prune((Dir) next);
				}

			}
		} catch (Throwable e) {

			e.printStackTrace();
		}

	}

}
