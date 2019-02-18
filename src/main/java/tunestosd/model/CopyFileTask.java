package tunestosd.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A task to copy file from source to target
 * 
 * @author paul
 *
 */
public class CopyFileTask implements ITask {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CopyFileTask.class);

	private File source;
	private File target;
	private String error;

	public CopyFileTask(File source, File target) {
		setSource(source);
		setTarget(target);
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}

	@Override
	public boolean execute() {
		log.debug("Copying file");
		setError(null);
		try {
			target.getParentFile().mkdirs();
			Files.copy(source.toPath(), target.toPath());
			return true;

		} catch (IOException e) {
			setError(e.getMessage());
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("COPY: %s %s", source, target);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
