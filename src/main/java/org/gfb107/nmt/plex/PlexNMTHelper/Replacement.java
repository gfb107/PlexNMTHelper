package org.gfb107.nmt.plex.PlexNMTHelper;

public class Replacement {
	private String from;
	private String to;
	private String playTo;

	public Replacement( String from, String to ) {
		if ( !from.endsWith( "/" ) ) {
			from = from + '/';
		}
		this.from = from;
		if ( !to.endsWith( "/" ) ) {
			to = to + '/';
		}
		this.to = to;
	}

	public void setPlayTo( String playTo ) {
		if ( !playTo.endsWith( "/" ) ) {
			playTo = playTo + '/';
		}
		this.playTo = playTo;
	}

	public boolean matches( String path ) {
		return path.startsWith( from );
	}

	public String convert( String path ) {
		return to + path.substring( from.length() );
	}

	public String convertPlay( String path ) {
		return playTo + path.substring( from.length() );
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String toString() {
		return "from " + from + " to " + to;
	}
}
