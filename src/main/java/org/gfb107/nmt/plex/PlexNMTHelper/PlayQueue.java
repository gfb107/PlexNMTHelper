package org.gfb107.nmt.plex.PlexNMTHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayQueue {
	private String id;
	private int itemOffset;
	List< Playable > playables = new ArrayList< Playable >();

	public PlayQueue( String id, int itemOffset ) {
		this.id = id;
		this.itemOffset = itemOffset;
	}

	public void add( Playable playable ) {
		playables.add( playable );
	}

	public Playable getCurrent() {
		if ( itemOffset < playables.size() ) {
			return playables.get( itemOffset );
		}
		return null;
	}

	public Playable next() {
		if ( itemOffset < playables.size() ) {
			itemOffset++;
		}
		return getCurrent();
	}
}
