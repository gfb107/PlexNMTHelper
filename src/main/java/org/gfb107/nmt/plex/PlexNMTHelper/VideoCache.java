package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.client.ClientProtocolException;

public class VideoCache {
	Map< String, Video > pathMap;
	Map< String, Video > keyMap;

	private PlexNMTHelper helper;
	private String unknownPath;
	private String unknownKey;

	public VideoCache( PlexNMTHelper helper ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, InterruptedException {
		this.helper = helper;

		load();
	}

	public void load() throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException, InterruptedException {
		System.out.println( "Building video cache" );

		List< Video > videos = helper.getServer().getKnownVideos();

		int size = videos.size() * 4 / 3;

		Map< String, Video > tempPathMap = new HashMap< String, Video >( size );
		Map< String, Video > tempKeyMap = new HashMap< String, Video >( size );

		for ( Video video : videos ) {
			helper.fix( video );
			tempPathMap.put( video.getFile(), video );
			tempKeyMap.put( video.getKey(), video );
		}

		pathMap = tempPathMap;
		keyMap = tempKeyMap;
	}

	private void updateGuid( Video video ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException {
		if ( video != null && video.getGuid() == null ) {
			helper.getServer().updateVideo( video );
		}
	}

	public Video getByPath( String path ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException,
			InterruptedException {
		Video video = pathMap.get( path );
		if ( video == null ) {
			if ( !path.equals( unknownPath ) ) {
				load();
				video = pathMap.get( path );
				if ( video == null ) {
					unknownPath = path;
				} else {
					unknownPath = null;
				}
			}
		} else {
			unknownPath = null;
		}

		updateGuid( video );
		return video;
	}

	public Video getByKey( String key ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException,
			InterruptedException {
		Video video = keyMap.get( key );
		if ( video == null ) {
			if ( !key.equals( unknownKey ) ) {
				load();
				video = keyMap.get( key );
				if ( video == null ) {
					unknownKey = key;
				} else {
					unknownKey = null;
				}
			}
		} else {
			unknownKey = null;
		}

		updateGuid( video );
		return video;
	}
}
