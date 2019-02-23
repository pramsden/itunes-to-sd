package de.ramsden.tunestosd.exec;

import java.io.File;

import com.gps.itunes.lib.parser.ItunesLibraryParsedData;
import com.gps.itunes.lib.parser.ItunesLibraryParser;
import com.gps.itunes.lib.parser.utils.PropertyManager;
import com.gps.itunes.lib.tasks.LibraryParser;

/**
 * Import itunes library on a thread. Callback on complete or fail
 * 
 * @author paul
 *
 */
public class TunesImporter {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TunesImporter.class);
	private File file;

	/**
	 * Import from file in new thread
	 * 
	 * @param file
	 */
	public TunesImporter(File file) {
		this.file = file;
	}

	public Thread start() {
		Thread t = new Thread("ImportItunes") {
			@Override
			public void run() {
				try {
					ItunesLibraryParser parser = new LibraryParser();
					parser.addParseConfiguration(PropertyManager.Property.LIBRARY_FILE_LOCATION_PROPERTY.getKey(),
							file.getAbsolutePath());

					ItunesLibraryParsedData library = parser.parse();

					onImportSuccess(library);
				} catch (Exception e) {
					onImportFailed(file, e);
				}
			}
		};

		t.start();

		return t;
	}

	/**
	 * Callback for import complete
	 * 
	 * @param library
	 */
	protected void onImportSuccess(ItunesLibraryParsedData library) {
		// overwrite
	}

	/**
	 * Callback for import failed
	 * 
	 * @param file
	 * @param e
	 */
	protected void onImportFailed(File file, Exception e) {
		// overwrite
	}
}
