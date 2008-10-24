package goofs.fs;

import java.nio.ByteBuffer;

public abstract class File extends Node {

	protected byte[] content = new byte[256];

	protected Dir parent;

	public File(Dir parent, String name, int mode, String content,
			String... xattrs) throws Exception {
		super(name, mode, xattrs);

		this.content = content.getBytes();

		this.parent = parent;
	}

	public byte[] getContent() {
		return content;
	}

	public Dir getParent() {
		return parent;
	}

	@Override
	public void remove() {

		synchronized (getParent().getLock()) {
			getParent().getFiles().remove(getName());
		}

		synchronized (getRootLock()) {
			nfiles--;
		}

	}

	@Override
	public void setName(String name) {

		synchronized (getParent().getLock()) {

			if (getParent().getFiles().containsKey(getName())) {

				getParent().getFiles().remove(getName());
				getParent().getFiles().put(name, this);
			}
		}

		super.setName(name);

	}

	public int read(ByteBuffer buf, long offset) {

		buf.put(content, (int) offset, Math.min(buf.remaining(), content.length
				- (int) offset));

		return 0;
	}

	public int write(boolean isWritepage, ByteBuffer buf, long offset) {

		int length = ((int) offset) + buf.remaining();

		if (content == null) {
			content = new byte[length];

		}

		else if (content.length < length) {

			byte[] ncontent = new byte[length];

			System.arraycopy(content, 0, ncontent, 0, content.length);

			content = ncontent;
		}

		buf.get(content, (int) offset, buf.remaining());

		return 0;
	}

	public abstract int save();

}
