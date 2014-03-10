package org.gfb107.nmt.plex.PlexNMTHelper;

import java.util.logging.Logger;

import nu.xom.Element;

public class NowPlayingMonitor implements Runnable {
	private static Logger logger = Logger.getLogger( NowPlayingMonitor.class.getName() );

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

	private Video lastVideo = null;

	public Video getLastVideo() {
		return lastVideo;
	}

	private Track lastTrack = null;

	public Track getLastTrack() {
		return lastTrack;
	}

	@Override
	public void run() {
		logger.info( "NowPlayingMonitor started" );
		while ( !stop ) {
			try {
				Thread.sleep( 1000 ); // sleep 1 second between iterations

				String state = null;

				Element container = nmt.sendCommand( "playback", "get_current_vod_info" );
				String returnValue = container.getFirstChildElement( "returnValue" ).getValue();

				Video video = null;

				if ( returnValue.equals( "0" ) ) {
					logger.fine( "A video is playing" );
					Element response = container.getFirstChildElement( "response" );
					String fullPath = response.getFirstChildElement( "fullPath" ).getValue();
					state = response.getFirstChildElement( "currentStatus" ).getValue();
					if ( state.equals( "play" ) ) {
						state = "playing";
					} else if ( state.equals( "pause" ) ) {
						state = "paused";
					}

					int currentTime = Integer.parseInt( response.getFirstChildElement( "currentTime" ).getValue() ) * 1000;
					int totalTime = Integer.parseInt( response.getFirstChildElement( "totalTime" ).getValue() ) * 1000;

					video = helper.getVideoByPath( fullPath );

					if ( lastVideo != null && lastVideo != video ) {
						logger.finer( "It's a different video than last time" );
						helper.updateTimeline( lastVideo, "stopped" );
						lastVideo = null;
					}

					if ( lastTrack != null ) {
						logger.fine( "There was a track playing last time" );
						helper.updateTimeline( lastTrack, "stopped" );
						lastTrack = null;
					}

					if ( video != null ) {
						video.setCurrentTime( currentTime );
						if ( video.getDuration() == 0 ) {
							video.setDuration( totalTime );
						}

						helper.updateTimeline( video, state );

						lastVideo = video;

						continue;
					}
				}

				Track track = null;

				if ( video == null ) {

					if ( lastVideo != null ) {
						logger.fine( "There was a video playing last time" );
						helper.updateTimeline( lastVideo, "stopped" );
						lastVideo = null;
					}

					container = nmt.sendCommand( "playback", "get_current_aod_info" );
					returnValue = container.getFirstChildElement( "returnValue" ).getValue();

					state = null;

					if ( returnValue.equals( "0" ) ) {
						logger.fine( "There's a track playing" );

						Element response = container.getFirstChildElement( "response" );
						String fullPath = response.getFirstChildElement( "fullPath" ).getValue();

						state = response.getFirstChildElement( "currentStatus" ).getValue();
						int currentTime = Integer.parseInt( response.getFirstChildElement( "currentTime" ).getValue() ) * 1000;
						int totalTime = Integer.parseInt( response.getFirstChildElement( "totalTime" ).getValue() ) * 1000;

						track = helper.getTrack( fullPath );

						if ( lastTrack != null && lastTrack != track ) {
							logger.fine( "It's a different track than last time" );
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
							if ( track.getDuration() == 0 ) {
								track.setDuration( totalTime );
							}

							helper.updateTimeline( track, state );

							lastTrack = track;

							continue;
						}
					}
				}

				if ( lastTrack != null ) {
					logger.fine( "No track this time, but there was a track last time" );
					helper.updateTimeline( lastTrack, "stopped" );
					lastTrack = null;
				}

			} catch ( Exception ex ) {
				ExceptionLogger.log( logger, ex );
			}
		}
		logger.info( "NowPlayingMonitor ending" );
	}
}
