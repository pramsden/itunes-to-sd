package tunestosd.model.org;

import com.gps.itunes.lib.items.tracks.Track;

public interface IExportListener {
	public boolean start();

	public boolean foundTracks(Track[] tracks);

	public boolean exportResult(JobResult result, int row);

	public boolean addedToLibrary(Track track);

	public void complete();
}
