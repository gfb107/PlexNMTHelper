package org.gfb107.nmt.plex.PlexNMTHelper;

public class Video extends Playable {
	public final static String type = "video";
	public final static String location = "fullScreenVideo";

	private String guid;
	private String httpFile;

	public Video( String containerKey, String key, String ratingKey, String title, String guid, int duration, String file, String httpFile ) {
		super( type, location, containerKey, key, ratingKey, title, file, duration );
		this.guid = guid;
		this.httpFile = httpFile;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid( String guid ) {
		this.guid = guid;
	}

	public String getHttpFile() {
		return httpFile;
	}

	public void setHttpFile( String httpFile ) {
		this.httpFile = httpFile;
	}
}
