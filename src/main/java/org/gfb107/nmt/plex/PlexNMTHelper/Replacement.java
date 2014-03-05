package org.gfb107.nmt.plex.PlexNMTHelper;

public class Replacement {
	private String from;
	private String to;

	public Replacement( String from, String to ) {
		this.from = from;
		this.to = to;
	}

	public boolean matches( String path ) {
		return path.startsWith( from );
	}

	public String convert( String path ) {
		return to + path.substring( from.length() );
	}
}
