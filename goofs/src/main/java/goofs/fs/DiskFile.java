package goofs.fs;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class DiskFile extends File {

	protected java.io.File disk;

	public DiskFile(Dir parent, String name, int mode) throws Exception {

		super(parent, name, mode, "");

		content = null;

		disk = java.io.File.createTempFile("goofs", null);

		disk.deleteOnExit();

	}

	public java.io.File getDisk() {
		return disk;
	}

	@Override
	public int getSize() {

		return (int) getDisk().length();
	}

	@Override
	public int write(boolean isWritepage, ByteBuffer buf, long offset) {

		super.write(isWritepage, buf, offset);

		setContent(this.content);

		this.content = null;

		return 0;
	}

	@Override
	public byte[] getContent() {
		byte[] result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[256];

		try {
			FileInputStream fis = new FileInputStream(getDisk());
			while (fis.read(buf) != -1) {
				baos.write(buf);
			}
			result = baos.toByteArray();
			baos.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void setContent(byte[] content) {

		try {
			FileOutputStream fos = new FileOutputStream(getDisk());
			fos.write(content);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void remove() {

		super.remove();

		getDisk().delete();

	}

}
