package org.gfb107.nmt.plex.PlexNMTHelper;

public class Track extends Playable {
	public static final String type = "music";
	public static final String location = "fullScreenMusic";

	public Track( String containerKey, String key, String ratingKey, String title, String file, int duration ) {
		super( type, location, containerKey, key, ratingKey, title, file, duration );
	}

	public String toString() {
		return getFile() + ": " + getTitle();
	}
}
