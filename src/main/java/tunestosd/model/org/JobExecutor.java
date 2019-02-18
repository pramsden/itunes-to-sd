package tunestosd.model.org;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import com.gps.itunes.lib.items.playlists.Playlist;
import com.gps.itunes.lib.items.playlists.PlaylistItem;
import com.gps.itunes.lib.items.tracks.Track;
import com.gps.itunes.lib.parser.ItunesLibraryParsedData;
import com.gps.itunes.lib.parser.utils.LocationDecoder;

import be.tarsos.transcoder.DefaultAttributes;
import be.tarsos.transcoder.Transcoder;

public class JobExecutor {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JobExecutor.class);
	private ItunesLibraryParsedData library;
	private ExportOptions options;
	private List<JobResult> results = new ArrayList<>();
	private List<IExportListener> listeners = new ArrayList<>();
	private boolean cancel;

	public JobExecutor(String pathItunes) {

	}

	public void addListener(IExportListener listener) {
		removeListener(listener);
		listeners.add(listener);
	}

	public void removeListener(IExportListener listener) {
		listeners.remove(listener);
	}

	protected void fireStart() {
		for (IExportListener iel : listeners) {
			cancel |= iel.start();
		}
	}

	protected void fireFoundTracks(Track[] tracks) {
		for (IExportListener iel : listeners) {
			cancel |= iel.foundTracks(tracks);
		}
	}

	protected void fireAddedToLibrary(Track track) {
		for (IExportListener iel : listeners) {
			cancel |= iel.addedToLibrary(track);
		}
	}

	protected void fireExportResult(JobResult result, int row) {
		for (IExportListener iel : listeners) {
			cancel |= iel.exportResult(result, row);
		}
	}

	protected void fireComplete() {
		for (IExportListener iel : listeners) {
			iel.complete();
		}
	}

	public Playlist[] getPlaylists() {
		return library.getAllPlaylists();
	}

	public void prepareStandardExport(ExportOptions options, String... playlists) throws Exception {
		List<Playlist> list = new ArrayList<>();
		for (String name : playlists) {
			for (Playlist play : library.getAllPlaylists()) {
				if (name.equalsIgnoreCase(play.getName())) {
					list.add(play);
				}
			}
		}
		prepareStandardExport(options, list);
	}

	public void prepareStandardExport(ExportOptions options, List<Playlist> playlists) throws Exception {
		this.options = options;

		if (options.getTargetFolder() == null || options.getTargetFolder().isDirectory() == false)
			throw new Exception(options.getTargetFolder() + " is not a folder");

		// File dirSongs = new File(options.getTargetFolder(), "Songs");
		File dirGenres = new File(options.getTargetFolder(), "Genres");
		File dirArtists = new File(options.getTargetFolder(), "Artists");
		// File dirBooks = new File(options.getTargetFolder(), "Books");
		// File dirPodcasts = new File(options.getTargetFolder(), "Podcasts");
		// File dirPlaylists = new File(options.getTargetFolder(), "Playlists");
		File dirLibrary = new File(options.getTargetFolder(), "Library");

		List<Track> allTracks = new ArrayList<>();
		long totalTime = 0;
		long totalSize = 0;
		for (Playlist list : playlists) {
			for (PlaylistItem item : list.getPlaylistItems()) {
				long id = item.getTrackId();
				Track track = getTrack(id);
				if (track != null) {
					allTracks.add(track);
					// totalSize += track.getAdditionalTrackInfo().
				}
			}
		}

		copyFiles(allTracks);

		List<String> genres = getAttributeList(allTracks, "Genre");
		Collections.sort(genres);
		if (options.isAddPlaylistGenre()) {
			create(dirLibrary, dirGenres, genres, allTracks, "Genre");
		}

		List<String> artists = getAttributeList(allTracks, "Album Artist", "Artist");
		Collections.sort(artists);
		if (options.isAddPlaylistArtist()) {
			create(dirLibrary, dirArtists, artists, allTracks, "Album Artist", "Artist");
		}

		System.out.println(artists.size());
	}

	private void copyFiles(List<Track> allTracks) {
		for (Track track : allTracks) {
			File path = copyTrack(track, options.getTargetFolder());
		}
	}

	private void create(File dirLibrary, File folder, List<String> names, List<Track> allTracks, String... attrName) {
		folder.mkdirs();
		for (String name : names) {
			File f = new File(folder, name + ".m3u");
			List<Track> tracks = getTracks(allTracks, name, attrName);
			createM3u(f, tracks, dirLibrary);
		}
	}

	private void createM3u(File m3u, List<Track> tracks, File dirLibrary) {
		System.out.println("Create M3U: " + m3u);
		m3u.getParentFile().mkdirs();
		m3u.delete();
		try (FileWriter out = new FileWriter(m3u, false)) {
			out.write("#EXTM3U\r\n\r\n");
			for (Track track : tracks) {
				File path = copyTrack(track, dirLibrary);
				if (path != null) {
					String stime = track.getAdditionalTrackInfo().getAdditionalInfo("Total Time");
					if (stime == null) {
						stime = "";
					} else {
						stime = Integer.toString(Integer.parseInt(stime) / 1000);
					}
					String artist = path.getParentFile().getParentFile().getName();
					String name = path.getName().replace(".mp3", "");
					out.write(String.format("#EXTINF;%s,%s\r\n%s\r\n\r\n", stime, artist, name,
							path.getAbsolutePath().substring(2)));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File copyTrack(Track track, File dirLibrary) {
		JobResult result = new JobResult(track);
		return copyTrack(result, dirLibrary);
	}

	private File copyTrack(JobResult result, File dirTarget) {

		results.add(result);

		File source = new File(LocationDecoder.decodeLocation(result.getTrack().getLocation()));

		if (source.exists()) {
			try {
				File mp3 = source;

				// not mp3?
				if (source.getName().endsWith(".mp3") == false) {
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
				File target = new File(dirTarget, path);
				log.debug("Copy " + source + " to " + target);

				if (targetOlder(target, source)) {
					boolean copied = false;
					target.getParentFile().mkdirs();
					if (source != mp3) {
						log.debug("Source is not mp3");
						File mp3Library = null;
						if (options.getPathMp3Library() != null) {
							mp3Library = new File(options.getPathMp3Library(), path);
							if (targetOlder(mp3Library, source)) {
								result.setDescription("Copied from MP3 library");
								log.debug("MP3 Source found in library");
								copyFile(mp3Library, target);
								copied = true;
								result.setStatus(JobStatus.Success);
							}
						}
						if (!copied) {
							result.setDescription("Transcoded to MP3");
							convertToMp3(source, target);
							if (target.exists()) {
								if (mp3Library != null) {
									log.debug("Adding converted mp3 to library");
									copyFile(target, mp3Library);
								}
								result.setStatus(JobStatus.Success);
							} else {
								result.setStatus(JobStatus.TranscoderFail);
							}
						}
					} else {
						copyFile(source, target);
						result.setDescription("File copied");
						result.setStatus(JobStatus.Success);
					}
				} else {
					result.setStatus(JobStatus.Success);
					result.setDescription("Target exists. No file copied");
				}

				result.setTarget(target);
				return target;
			} catch (Exception e) {
				log.error("Fail", e);
				result.setStatus(JobStatus.Fail);
				result.setDescription("Copy failed: " + e.getMessage());
			}
		} else {
			log.error("Source does not exist");
			result.setStatus(JobStatus.Fail);
			result.setDescription("Source does not exist");
		}
		return null;
	}

	private boolean targetOlder(File target, File source) {
		if (target.exists() == false)
			return true;
		return target.lastModified() < source.lastModified();
	}

	private void copyFile(File source, File target) throws IOException {
		target.mkdirs();
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	private void convertToMp3(File source, File target) throws Exception {
		try {
			log.debug("Attempt transcoding of " + source);
			Transcoder.transcode(source.getAbsolutePath(), target.getAbsolutePath(),
					DefaultAttributes.MP3_192KBS_STEREO_44KHZ);

			AudioFile f = AudioFileIO.read(source);
			Tag tag = f.getTag();
			String artist = tag.getFirst(FieldKey.ARTIST);
			String album = tag.getFirst(FieldKey.ALBUM);
			String title = tag.getFirst(FieldKey.TITLE);
			String comment = tag.getFirst(FieldKey.COMMENT);
			String year = tag.getFirst(FieldKey.YEAR);
			String track = tag.getFirst(FieldKey.TRACK);
			String discno = tag.getFirst(FieldKey.DISC_NO);
			String composer = tag.getFirst(FieldKey.COMPOSER);
			String artistSort = tag.getFirst(FieldKey.ARTIST_SORT);
			Artwork art = tag.getFirstArtwork();

			f = AudioFileIO.read(target);
			tag = f.getTag();
			tag.setField(FieldKey.ARTIST, artist);
			tag.setField(FieldKey.ALBUM, album);
			tag.setField(FieldKey.TITLE, title);
			tag.setField(FieldKey.COMMENT, comment);
			tag.setField(FieldKey.YEAR, year);
			if (StringUtils.isNumeric(track))
				tag.setField(FieldKey.TRACK, track);
			if (StringUtils.isNumeric(discno))
				tag.setField(FieldKey.DISC_NO, discno);
			tag.setField(FieldKey.COMPOSER, composer);
			tag.setField(FieldKey.ARTIST_SORT, artistSort);
			if (art != null)
				tag.setField(art);
			f.commit();
		} catch (Exception e) {
			log.error("Convert to mp3 failed.", e);
			throw e;
		}
	}

	private List<Track> getTracks(List<Track> allTracks, String name, String... attrNames) {
		List<Track> tracks = new ArrayList<>();
		for (Track track : allTracks) {
			for (String attrName : attrNames) {
				String attr = track.getAdditionalTrackInfo().getAdditionalInfo(attrName);
				if (attr != null && attr.equalsIgnoreCase(name)) {
					tracks.add(track);
					break;
				}
			}
		}
		return tracks;
	}

	private List<String> getAttributeList(List<Track> tracks, String... names) {
		List<String> attrs = new ArrayList<>();
		for (Track track : tracks) {
			String attr = null;
			for (String name : names) {
				attr = track.getAdditionalTrackInfo().getAdditionalInfo(name);
				if (attr != null)
					break;
			}
			if (attr == null)
				attr = "Unknown";
			if (attrs.contains(attr) == false) {
				attr = attr.replace(':', ',').replace(',', ',').replace('/', ',').replace('\\', ',').replace('.', ',')
						.replace(';', ',').replace(",,", ",");
				attrs.add(attr);
			}
		}
		return attrs;
	}

	public Track getTrack(long id) {
		for (Track track : library.getAllTracks()) {
			if (track.getTrackId() == id)
				return track;
		}
		return null;
	}

	public void prepareStandardExport(ExportOptions options, File targetFolder) throws Exception {
		List<Playlist> ignorePlaylists = new ArrayList<>();

		List<Playlist> list = new ArrayList<>();
		for (Playlist playlist : getPlaylists()) {
			if (ignorePlaylists.contains(playlist) == false) {
				list.add(playlist);
			}
		}
		prepareStandardExport(options, list);
	}

	public List<JobItem> compare(JobDescription jobDescription) {
		List<JobItem> jobs = new ArrayList<>();

		Playlist[] playlists = library.getAllPlaylists();
		Track[] tracks = library.getAllTracks();

		for (Track track : tracks) {
			if (compare(jobDescription, track)) {
				jobs.add(new JobItem(track, jobDescription));
			}
		}

		System.out.println(jobs.size());

		return jobs;
	}

	private boolean compare(JobDescription jobDescription, Track track) {
		boolean include = false;
		boolean exclude = false;

		String genre = track.getAdditionalTrackInfo().getAdditionalInfo("Genre");
		include |= compareAttribute(genre, jobDescription.getIncludeGenres());
		exclude |= compareAttribute(genre, jobDescription.getExcludeGenres());

		String artist = track.getAdditionalTrackInfo().getAdditionalInfo("Artist");
		include |= compareAttribute(artist, jobDescription.getIncludeArtists());
		exclude |= compareAttribute(artist, jobDescription.getExcludeArtists());

		String album = track.getAdditionalTrackInfo().getAdditionalInfo("Album");
		include |= compareAttribute(album, jobDescription.getIncludeAlbums());
		exclude |= compareAttribute(album, jobDescription.getExcludeAlbums());

		String rating = track.getAdditionalTrackInfo().getAdditionalInfo("Rating");
		include |= compareIntAttribute(rating, jobDescription.getMinRating());

		String plays = track.getAdditionalTrackInfo().getAdditionalInfo("Play Count");
		include |= compareIntAttribute(plays, jobDescription.getMinRating());

		String decodedLocation = LocationDecoder.decodeLocation(track.getLocation());
		include |= comparePathAttribute(decodedLocation, jobDescription.getIncludePaths());
		exclude |= comparePathAttribute(decodedLocation, jobDescription.getExcludePaths());

		return include & !exclude;
	}

	private boolean compareIntAttribute(String rating, Integer minRating) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean comparePathAttribute(String value, List<String> includePaths) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean compareAttribute(String value, List<String> list) {
		if (value == null)
			value = "";

		for (String match : list) {
			match = match.replace("*", ".*").replace("?", ".");
			if (value.matches("(?i)" + match)) {
				return true;
			}
		}
		return false;
	}

	public void export(ExportOptions exportOptions, List<JobResult> data) throws Exception {
		this.options = exportOptions;

		if (options.getTargetFolder() == null || options.getTargetFolder().isDirectory() == false)
			throw new Exception(options.getTargetFolder() + " is not a folder");

		int row = 0;
		for (JobResult jobResult : data) {
			fireExportResult(jobResult, row);
			copyTrack(jobResult, options.getTargetFolder());
			fireExportResult(jobResult, row++);
			Thread.sleep(0);
		}
	}

	private void performJob(JobResult jobResult) {
	}

	/**
	 * Create all playlists. Ignore standard playlists unless they were explicitly
	 * selected
	 * 
	 * @param exportOptions
	 * @param playlists
	 * @param results
	 */
	public void createPlaylists(ExportOptions exportOptions, List<Playlist> playlists, List<JobResult> results) {
		String[] stdLists = new String[] { "Library", "Downloaded", "Music", "Films", "TV Programmes", "Podcasts",
				"Audiobooks" };

		for (Playlist list : library.getAllPlaylists()) {
			String name = list.getName();
			boolean skip = false;
			for (String std : stdLists) {
				if (std.equals(name)) {
					skip = true;
					break;
				}
			}
			if (!skip && !playlists.contains(list))
				playlists.add(list);
		}

		for (Playlist list : playlists) {
			createM3u(list, results, exportOptions);
		}
	}

	/**
	 * Create M3U playlist and store in {base}/Playlists. Only add files which are
	 * actually present.
	 * 
	 * @param list
	 * @param results
	 * @param exportOptions
	 */
	private void createM3u(Playlist list, List<JobResult> results, ExportOptions exportOptions) {
		File m3u = new File(exportOptions.getTargetFolder(), "Playlists");
		m3u = new File(m3u, list.getName() + ".m3u");
		m3u.getParentFile().mkdirs();

		try (FileWriter out = new FileWriter(m3u, false)) {
			out.write("#EXTM3U\r\n");
			for (PlaylistItem item : list.getPlaylistItems()) {
				long id = item.getTrackId();

				for (JobResult result : results) {
					if (result.getTrack().getTrackId() == id) {
						String stime = result.getTrack().getAdditionalTrackInfo().getAdditionalInfo("Total Time");
						if (stime == null) {
							stime = "";
						} else {
							stime = Integer.toString(Integer.parseInt(stime) / 1000);
						}
						File path = result.getTarget();
						String artist = path.getParentFile().getParentFile().getName().replace('-', '_');
						String name = path.getName().replace(".mp3", "").replace('-', '_');
						out.write(String.format("#EXTINF:%s,%s - %s\r\n..%s\r\n", stime, artist, name,
								path.getAbsolutePath().substring(2)));
					}
				}
			}
		} catch (Exception e) {

		}
	}
}
