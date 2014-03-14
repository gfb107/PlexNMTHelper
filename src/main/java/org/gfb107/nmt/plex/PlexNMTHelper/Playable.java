package org.gfb107.nmt.plex.PlexNMTHelper;

public abstract class Playable {
	public Playable( String containerKey, String key, String ratingKey, String title, String file, int duration ) {
		this.containerKey = containerKey;
		this.key = key;
		this.ratingKey = ratingKey;
		this.title = title;
		this.file = file;
		this.duration = duration;
	}

	private String containerKey;
	private String key;
	private String ratingKey;
	private String title;
	private String file;
	private String playFile;
	private int duration;
	private int currentTime;

	public String getContainerKey() {
		return containerKey;
	}

	public String getKey() {
		return key;
	}

	public String getRatingKey() {
		return ratingKey;
	}

	public String getTitle() {
		return title;
	}

	public String getFile() {
		return file;
	}

	public void setFile( String file ) {
		this.file = file;
	}

	public String getPlayFile() {
		return playFile;
	}

	public void setPlayFile( String playFile ) {
		this.playFile = playFile;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration( int duration ) {
		this.duration = duration;
	}

	public int getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime( int time ) {
		this.currentTime = time;
	}

	public abstract String getLocation();

	public abstract String getType();
}
