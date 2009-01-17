package goofs.fs;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public void flush() {

		if (this.content != null) {
			setContent(this.content);
			this.content = null;
		}

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
			FileOutputStream fos = new FileOutputStream(getDisk(), getDisk()
					.length() > 0);
			fos.write(content);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setContent(InputStream is) throws Exception {

		FileOutputStream fos = new FileOutputStream(getDisk());

		try {

			byte[] buff = new byte[256];
			int bytesRead = 0;

			while ((bytesRead = is.read(buff)) != -1) {
				fos.write(buff, 0, bytesRead);

			}
		} finally {

			fos.close();
			is.close();
		}

	}

	@Override
	public void remove() {

		super.remove();

		getDisk().delete();

	}

}
