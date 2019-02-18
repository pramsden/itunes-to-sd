package tunestosd.model.original;

import java.util.ArrayList;
import java.util.List;

public class JobDescription {
	private String name;
	private String itunesFile;
	private String targetFolder;
	private List<String> includeGenres = new ArrayList<>();
	private List<String> excludeGenres = new ArrayList<>();
	private List<String> includePaths = new ArrayList<>();
	private List<String> excludePaths = new ArrayList<>();
	private List<String> includeArtists = new ArrayList<>();
	private List<String> excludeArtists = new ArrayList<>();
	private List<String> includeAlbums = new ArrayList<>();
	private List<String> excludeAlbums = new ArrayList<>();
	private Integer minRating = null;
	private Integer minPlayCount = null;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getItunesFile() {
		return itunesFile;
	}
	public void setItunesFile(String itunesFile) {
		this.itunesFile = itunesFile;
	}
	public List<String> getIncludeGenres() {
		return includeGenres;
	}
	public void setIncludeGenres(List<String> includeGenres) {
		this.includeGenres = includeGenres;
	}
	public List<String> getExcludeGenres() {
		return excludeGenres;
	}
	public void setExcludeGenres(List<String> excludeGenres) {
		this.excludeGenres = excludeGenres;
	}
	public List<String> getIncludePaths() {
		return includePaths;
	}
	public void setIncludePaths(List<String> includePaths) {
		this.includePaths = includePaths;
	}
	public List<String> getExcludePaths() {
		return excludePaths;
	}
	public void setExcludePaths(List<String> excludePaths) {
		this.excludePaths = excludePaths;
	}
	public List<String> getIncludeArtists() {
		return includeArtists;
	}
	public void setIncludeArtists(List<String> includeArtists) {
		this.includeArtists = includeArtists;
	}
	public List<String> getExcludeArtists() {
		return excludeArtists;
	}
	public void setExcludeArtists(List<String> excludeArtists) {
		this.excludeArtists = excludeArtists;
	}
	public List<String> getIncludeAlbums() {
		return includeAlbums;
	}
	public void setIncludeAlbums(List<String> includeAlbums) {
		this.includeAlbums = includeAlbums;
	}
	public List<String> getExcludeAlbums() {
		return excludeAlbums;
	}
	public void setExcludeAlbums(List<String> excludeAlbums) {
		this.excludeAlbums = excludeAlbums;
	}
	public String getTargetFolder() {
		return targetFolder;
	}
	public void setTargetFolder(String targetFolder) {
		this.targetFolder = targetFolder;
	}
	public Integer getMinRating() {
		return minRating;
	}
	public void setMinRating(Integer minRating) {
		this.minRating = minRating;
	}
	public Integer getMinPlayCount() {
		return minPlayCount;
	}
	public void setMinPlayCount(Integer minPlayCount) {
		this.minPlayCount = minPlayCount;
	}
}
