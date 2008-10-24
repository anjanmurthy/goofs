package goofs.fs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHandle {

	protected static final Log log = LogFactory.getLog(FileHandle.class);

	protected Node node;

	public FileHandle(Node node) {
		this.node = node;
		log.debug("  " + this + " created");
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int release() {
		int result = ((File) getNode()).save();

		log.debug("  " + this + " released");

		return result;
	}

	protected void finalize() {
		log.debug("  " + this + " finalized");
	}

	public String toString() {
		return "FH[" + getNode() + ", hashCode=" + hashCode() + "]";
	}

}
