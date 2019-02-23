package de.ramsden.tunestosd.model;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import be.tarsos.transcoder.DefaultAttributes;
import be.tarsos.transcoder.Transcoder;

/**
 * A task which converts audio file to MP3
 * 
 * @author paul
 *
 */
public class ConvertToMp3Task implements ITask {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConvertToMp3Task.class);

	private File source;
	private File target;
	private String error;

	public ConvertToMp3Task(File source, File target) {
		setSource(source);
		setTarget(target);
	}

	public File getSource() {
		return source;
	}

	public void setSource(File source) {
		this.source = source;
	}

	public File getTarget() {
		return target;
	}

	public void setTarget(File target) {
		this.target = target;
	}

	@Override
	public boolean execute() {
		log.debug("Converting to Mp3");

		return convertToMp3(source, target);
	}

	private boolean convertToMp3(File source, File target) {
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

			return true;
		} catch (Exception e) {
			log.error("Convert to mp3 failed.", e);
			setError(e.getMessage());
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("CONVERT: %s %s", source, target);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
