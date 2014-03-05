package org.gfb107.nmt.plex.PlexNMTHelper;

import nu.xom.Element;

public class NowPlayingMonitor implements Runnable {

	public NowPlayingMonitor( PlexNMTHelper helper, NetworkedMediaTank nmt ) {
		this.helper = helper;
		this.nmt = nmt;
	}

	private PlexNMTHelper helper;
	private NetworkedMediaTank nmt;

	private boolean stop = false;

	public void setStop( boolean stop ) {
		this.stop = stop;
	}

	@Override
	public void run() {
		System.out.println( "NowPlayingMonitor started" );
		Video lastVideo = null;
		Track lastTrack = null;
		while ( !stop ) {
			try {
				Thread.sleep( 1000 ); // sleep 1 second between iterations

				String state = null;

				Element container = nmt.sendCommand( "playback", "get_current_vod_info" );
				String returnValue = container.getFirstChildElement( "returnValue" ).getValue();

				Video video = null;

				if ( returnValue.equals( "0" ) ) {

					Element response = container.getFirstChildElement( "response" );
					String fullPath = response.getFirstChildElement( "fullPath" ).getValue();
					state = response.getFirstChildElement( "currentStatus" ).getValue();
					if ( state.equals( "play" ) ) {
						state = "playing";
					} else if ( state.equals( "pause" ) ) {
						state = "paused";
					}

					int currentTime = Integer.parseInt( response.getFirstChildElement( "currentTime" ).getValue() ) * 1000;

					video = helper.getVideoByPath( fullPath );

					if ( lastVideo != null && lastVideo != video ) {
						helper.updateTimeline( lastVideo, "stopped" );
						lastVideo = null;
					}

					if ( lastTrack != null ) {
						helper.updateTimeline( lastTrack, "stopped" );
						lastTrack = null;
					}

					if ( video != null ) {
						video.setCurrentTime( currentTime );

						helper.updateTimeline( video, state );

						lastVideo = video;

						continue;
					}
				}

				Track track = null;

				if ( video == null ) {

					if ( lastVideo != null ) {
						helper.updateTimeline( lastVideo, "stopped" );
						lastVideo = null;
					}

					container = nmt.sendCommand( "playback", "get_current_aod_info" );
					returnValue = container.getFirstChildElement( "returnValue" ).getValue();

					state = null;

					if ( returnValue.equals( "0" ) ) {

						Element response = container.getFirstChildElement( "response" );
						String fullPath = response.getFirstChildElement( "fullPath" ).getValue();

						state = response.getFirstChildElement( "currentStatus" ).getValue();
						int currentTime = Integer.parseInt( response.getFirstChildElement( "currentTime" ).getValue() ) * 1000;

						track = helper.getTrack( fullPath );

						if ( lastTrack != null && lastTrack != track ) {
							helper.updateTimeline( lastTrack, "stopped" );
							lastTrack = null;
						}

						if ( track != null ) {
							if ( state.equals( "play" ) ) {
								state = "playing";
							} else if ( state.equals( "pause" ) ) {
								state = "paused";
							}

							track.setCurrentTime( currentTime );

							helper.updateTimeline( track, state );

							lastTrack = track;

							continue;
						}
					}
				}

				if ( lastTrack != null ) {
					helper.updateTimeline( lastTrack, "stopped" );
					lastTrack = null;
				}

			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}
		System.out.println( "NowPlayingMonitor ending" );
	}
}
