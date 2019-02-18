package tunestosd.model.org;

import java.io.File;

import com.gps.itunes.lib.items.playlists.Playlist;

public class ExportOptions {
	private boolean addPlaylistGenre, addPlaylistArtist;
	private File pathMp3Library;
	private File targetFolder;
	private Playlist[] playlists;

	public boolean isAddPlaylistGenre() {
		return addPlaylistGenre;
	}

	public void setAddPlaylistGenre(boolean addPlaylistGenre) {
		this.addPlaylistGenre = addPlaylistGenre;
	}

	public boolean isAddPlaylistArtist() {
		return addPlaylistArtist;
	}

	public void setAddPlaylistArtist(boolean addPlaylistArtist) {
		this.addPlaylistArtist = addPlaylistArtist;
	}

	public File getPathMp3Library() {
		return pathMp3Library;
	}

	public void setPathMp3Library(File pathMp3Library) {
		this.pathMp3Library = pathMp3Library;
	}

	public File getTargetFolder() {
		return targetFolder;
	}

	public void setTargetFolder(File targetFolder) {
		this.targetFolder = targetFolder;
	}

	public Playlist[] getPlaylists() {
		return playlists;
	}

	public void setPlaylists(Playlist[] playlists) {
		this.playlists = playlists;
	}

}
