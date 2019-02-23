package de.ramsden.tunestosd.model;

import java.io.File;

/**
 * Options for a job. More than one job can be saved and called as necessary
 * 
 * @author paul
 *
 */
public class JobOptions {
	private String name;
	private File itunes;
	private File targetDir;
	private File mp3Library;
	private boolean convertToMp3;
	private String[] playlistNames;
	private boolean onlyCopyIfNewer;

	/**
	 * Convert all files to Mp3 if possible
	 * 
	 * @return
	 */
	public boolean isConvertToMp3() {
		return convertToMp3;
	}

	public void setConvertToMp3(boolean convertToMp3) {
		this.convertToMp3 = convertToMp3;
	}

	/**
	 * Job name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ITunes file
	 * 
	 * @return
	 */
	public File getItunes() {
		return itunes;
	}

	public void setItunes(File itunes) {
		this.itunes = itunes;
	}

	/**
	 * Target directory. Usually on an SD card
	 * 
	 * @return
	 */
	public File getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(File targetDir) {
		this.targetDir = targetDir;
	}

	/**
	 * Location to store converted Mp3 files. If null, converted files will not be
	 * stored
	 * 
	 * @return
	 */
	public File getMp3LibraryDir() {
		return mp3Library;
	}

	public void setMp3LibraryDir(File mp3LibraryDir) {
		this.mp3Library = mp3LibraryDir;
	}

	/**
	 * Names of selected playlists
	 * 
	 * @return
	 */
	public String[] getPlaylistNames() {
		return playlistNames;
	}

	public void setPlaylistNames(String[] playlistNames) {
		this.playlistNames = playlistNames;
	}

	public boolean isOnlyCopyIfNewer() {
		return onlyCopyIfNewer;
	}

	public void setOnlyCopyIfNewer(boolean onlyCopyIfNewer) {
		this.onlyCopyIfNewer = onlyCopyIfNewer;
	}
}
