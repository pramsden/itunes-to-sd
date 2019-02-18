package tunestosd.model;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class Helper {
	private Helper() {
	}

	public static boolean isRemoveableDrive(File file) {
		return FileSystemView.getFileSystemView().isFloppyDrive(file);
	}
}
