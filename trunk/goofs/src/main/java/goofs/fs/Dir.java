package goofs.fs;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Dir extends Node {

	protected Map<String, Node> files = new LinkedHashMap<String, Node>();

	protected Dir parent;

	public Dir(Dir parent, String name, int mode, String... xattrs) {
		super(name, mode, xattrs);

		this.parent = parent;
	}

	public abstract int createChild(String name, boolean isDir);

	public abstract int createTempChild(String name);

	public abstract int createChildFromExisting(String name, Node child);

	public void add(Node n) {

		synchronized (getLock()) {
			files.put(n.name, n);
		}
	}

	public String toString() {
		return super.toString() + " with " + files.size() + " files";
	}

	public Dir getParent() {
		return parent;
	}

	public Map<String, Node> getFiles() {
		return files;
	}

	public void setFiles(Map<String, Node> files) {
		this.files = files;
	}

	@Override
	public void remove() {

		synchronized (getLock()) {

			Collection<Node> childs = getFiles().values();

			for (Node child : childs) {

				child.remove();
			}

			getParent().getFiles().remove(getName());

			synchronized (getRootLock()) {
				nfiles--;

			}

		}

	}

}
