package tunestosd.model.org;

import java.io.File;

import com.gps.itunes.lib.items.tracks.Track;

public class JobResult {
	private String description, action;
	private Track track;
	private JobStatus status;
	private File target;

	public JobResult(Track t) {
		track = t;
		status = JobStatus.Open;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JobResult) {
			return ((JobResult) obj).track.equals(track);
		}
		return false;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}
}
