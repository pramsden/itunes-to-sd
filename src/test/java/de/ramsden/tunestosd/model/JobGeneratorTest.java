package de.ramsden.tunestosd.model;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.gps.itunes.lib.parser.ItunesLibraryParsedData;

import de.ramsden.tunestosd.exec.JobGenerator;
import de.ramsden.tunestosd.exec.TunesImporter;
import de.ramsden.tunestosd.model.ITask;
import de.ramsden.tunestosd.model.JobOptions;

public class JobGeneratorTest {

	@Test
	public void testGenerate() throws InterruptedException {
		final JobOptions options = new JobOptions();
		options.setMp3LibraryDir(new File("P:\\MP3LIBRARY"));
		options.setTargetDir(new File("D:\\temp\\test"));
		options.setPlaylistNames(new String[] { "Car Upbeat" });
		options.setItunes(new File("C:/Users/paul/Music/iTunes/iTunes Music Library.xml"));
		options.setOnlyCopyIfNewer(false);

		new TunesImporter(options.getItunes()) {
			@Override
			protected void onImportFailed(File file, Exception e) {
				System.err.println(e);
			}

			@Override
			protected void onImportSuccess(ItunesLibraryParsedData library) {
				JobGenerator gen = new JobGenerator();

				List<ITask> tasks = gen.generate(options, library);

				for (ITask task : tasks) {
					System.out.println(task);
					task.execute();
				}
			}
		}.start().join();

	}

}
