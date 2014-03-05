package org.gfb107.nmt.plex.PlexNMTHelper;

public class Track extends Playable {
	public Track( String containerKey, String key, String ratingKey, String title, String file, int duration ) {
		super( containerKey, key, ratingKey, title, file, duration );
		this.title = title;
	}

	private String title;

	public String getTitle() {
		return title;
	}

	public String getLocation() {
		return "fullScreenMusic";
	}

	public String getType() {
		return "music";
	}

	public String toString() {
		return getFile() + ": " + title;
	}
}
