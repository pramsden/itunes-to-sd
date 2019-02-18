package tunestosd.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.gps.itunes.lib.items.playlists.Playlist;
import com.gps.itunes.lib.items.playlists.PlaylistItem;
import com.gps.itunes.lib.items.tracks.Track;
import com.gps.itunes.lib.parser.ItunesLibraryParsedData;
import com.gps.itunes.lib.parser.utils.LocationDecoder;

public class JobGenerator {
	private static final Logger log = Logger.getLogger(ConvertToMp3Task.class);

	private ItunesLibraryParsedData library;

	/**
	 * Generate a list of tasks to perform.
	 * 
	 * @param options
	 * @param library
	 * @return
	 */
	public List<ITask> generate(JobOptions options, ItunesLibraryParsedData library) {
		this.library = library;

		List<Playlist> playlists = new ArrayList<>();
		for (String name : options.getPlaylistNames()) {
			for (Playlist play : library.getAllPlaylists()) {
				if (name.equalsIgnoreCase(play.getName())) {
					playlists.add(play);
				}
			}
		}

		return generate(options, playlists);
	}

	private List<ITask> generate(JobOptions options, List<Playlist> playlists) {
		List<ITask> tasks = new ArrayList<>();

		for (Playlist list : playlists) {
			for (PlaylistItem item : list.getPlaylistItems()) {
				long id = item.getTrackId();
				Track track = getTrackById(id);
				if (track != null) {
					tasks.addAll(createTasks(track, options));
				} else {
					log.error("Could not find track with id: " + id);
				}
			}
		}

		return tasks;
	}

	/**
	 * Create tasks for this track. If source is not mp3 then add task to convert to
	 * mp3 if not in mp3 library
	 * 
	 * @param track
	 * @param options
	 * @return
	 */
	private Collection<? extends ITask> createTasks(Track track, JobOptions options) {
		List<ITask> tasks = new ArrayList<>();

		File source = new File(LocationDecoder.decodeLocation(track.getLocation()));

		if (source.exists()) {
			try {
				File mp3 = source; // assume mp3
				File mp3Library = null; // cached converted mp3

				boolean sourceIsMp3 = source.getName().endsWith(".mp3");

				// not mp3?
				if (!sourceIsMp3) {
					String path = source.getAbsolutePath();
					int i = path.lastIndexOf('.');
					if (i >= 0) {
						path = path.substring(0, i) + ".mp3";
					} else {
						path = path + ".mp3";
					}
					mp3 = new File(path);
				}

				// create target file based on MP3 name
				String type = mp3.getParentFile().getParentFile().getParentFile().getName();
				String artist = mp3.getParentFile().getParentFile().getName();
				String album = mp3.getParentFile().getName();
				String path = String.format("/%s/%s/%s/%s", type, artist, album, mp3.getName());
				File target = new File(options.getTargetDir(), path);

				boolean useCache = false;

				// look for cached mp3?
				if (!sourceIsMp3 && options.getMp3LibraryDir() != null) {
					mp3Library = new File(options.getMp3LibraryDir(), path);
					useCache = true;

					mp3 = mp3Library;

					// update cache?
					if (isTargetMissingOrOlder(mp3Library, source)) {
						ITask task = new ConvertToMp3Task(source, mp3Library);
						tasks.add(task);
					}
				}

				if (!options.isOnlyCopyIfNewer() || isTargetMissingOrOlder(target, useCache ? mp3Library : mp3)) {
					tasks.add(new CopyFileTask(mp3, target));
				}

			} catch (Exception e) {
				log.error("Could not create task for track: " + source);
			}
		} else {
			log.error("Track source does not exist: " + source);
		}

		return tasks;
	}

	/**
	 * Find track in library using id
	 * 
	 * @param id
	 * @return
	 */
	private Track getTrackById(long id) {
		for (Track track : library.getAllTracks()) {
			if (track.getTrackId() == id)
				return track;
		}
		return null;
	}

	/**
	 * Return true if target is older than source or does not exist
	 * 
	 * @param target
	 * @param source
	 * @return
	 */
	private static boolean isTargetMissingOrOlder(File target, File source) {
		if (!target.exists())
			return true;
		return target.lastModified() < source.lastModified();
	}

}
