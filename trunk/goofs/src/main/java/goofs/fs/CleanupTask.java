package goofs.fs;

import goofs.Fetchable;

import java.util.TimerTask;

import com.google.gdata.util.ResourceNotFoundException;

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

			for (Node next : root.getFiles().values()) {

				if (next instanceof Fetchable) {

					try {

						((Fetchable) next).fetch();
					}

					catch (Exception ex) {

						if (ex instanceof ResourceNotFoundException
								|| ex.getCause() instanceof ResourceNotFoundException) {

							next.remove();
							continue;
						}

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
