package goofs.fs;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class Node {

	protected static int nfiles = 0;
	protected String name;
	protected int mode;
	protected Map<String, byte[]> xattrs = new HashMap<String, byte[]>();
	protected int createTime;
	protected int modifyTime;
	protected int accessTime;
	protected Object lock = new Object();
	protected static ResourceBundle resourceBundle = ResourceBundle
			.getBundle("goofs");

	public abstract Node getParent();

	public Node(String name, int mode, String... xattrs) {
		this.name = name;
		this.mode = mode;
		int time = (int) (System.currentTimeMillis() / 1000L);
		this.createTime = time;
		this.accessTime = time;
		this.modifyTime = time;

		for (int i = 0; i < xattrs.length - 1; i += 2)
			this.xattrs.put(xattrs[i], xattrs[i + 1].getBytes());

		synchronized (getRootLock()) {
			nfiles++;
		}
	}

	public Object getLock() {
		return lock;
	}

	public Object getRootLock() {

		return getParent() == null ? getLock() : getParent().getRootLock();
	}

	public String toString() {
		return getClass().getName() + "." + getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {

		if (getParent() != null) {
			synchronized (getParent().getLock()) {

				if (((Dir) getParent()).getFiles().containsKey(getName())) {

					((Dir) getParent()).getFiles().remove(getName());
					((Dir) getParent()).getFiles().put(name, this);
				}
			}
		}

		this.name = name;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(int modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(int accessTime) {
		this.accessTime = accessTime;
	}

	public void updateAccessTime() {
		setAccessTime((int) (System.currentTimeMillis() / 1000L));
	}

	public void updateModifyTime() {
		setModifyTime((int) (System.currentTimeMillis() / 1000L));
	}

	public abstract void remove();

	public abstract int delete();

	public abstract int rename(Dir newParent, String name);

}
