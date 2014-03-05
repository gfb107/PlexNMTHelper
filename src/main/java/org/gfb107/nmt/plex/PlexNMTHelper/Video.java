package org.gfb107.nmt.plex.PlexNMTHelper;

public class Video extends Playable {
	private String guid;
	private String httpFile;

	public Video( String containerKey, String ratingKey, String title, String guid, int duration, String file, String httpFile ) {
		super( containerKey, containerKey, ratingKey, title, file, duration );
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

	public String getLocation() {
		return "fullScreenVideo";
	}

	public String getType() {
		return "video";
	}
}
