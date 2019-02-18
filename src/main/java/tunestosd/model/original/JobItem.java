package tunestosd.model.original;

import com.gps.itunes.lib.items.tracks.Track;

public class JobItem {
	private Track track;
	private JobAction action;

	public JobItem(Track track, JobDescription job) {
		this.track = track;
	}

	public JobAction getAction() {
		return action;
	}

	public void setAction(JobAction action) {
		this.action = action;
	}
}
