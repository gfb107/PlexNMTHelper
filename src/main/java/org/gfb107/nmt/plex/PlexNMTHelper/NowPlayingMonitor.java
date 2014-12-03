package org.gfb107.nmt.plex.PlexNMTHelper;

import java.util.logging.Logger;

import nu.xom.Element;

public class NowPlayingMonitor implements Runnable {
	private Logger logger = Logger.getLogger( NowPlayingMonitor.class.getName() );

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

	private Playable getCurrent() {
		PlayQueue queue = helper.getQueue();
		if ( queue == null ) {
			return null;
		}
		return queue.getCurrent();
	}

	private Playable getNext() {
		PlayQueue queue = helper.getQueue();
		if ( queue == null ) {
			return null;
		}
		return queue.next();
	}

	@Override
	public void run() {
		logger.info( "NowPlayingMonitor started" );
		while ( !stop ) {
			try {
				Thread.sleep( 1000 ); // sleep 1 second between iterations

				Playable playable = getCurrent();
				if ( playable == null ) {
					logger.fine( "Queue finished" );
					lastVideo = null;
					lastTrack = null;
					continue;
				}

				String state = null;

				Element container = nmt.sendCommand( "playback", "get_current_vod_info" );
				String returnValue = container.getFirstChildElement( "returnValue" ).getValue();

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

					boolean sameVideo = playable.getPlayFile().equals( fullPath );

					if ( lastVideo != null && !sameVideo ) {
						logger.finer( "It's a different video than last time" );
						lastVideo.setState( "stopped" );
						helper.updateTimeline( lastVideo );
						lastVideo = null;
					}

					if ( lastTrack != null ) {
						logger.fine( "There was a track playing last time" );
						lastTrack.setState( "stopped" );
						helper.updateTimeline( lastTrack );
						lastTrack = null;
					}

					if ( sameVideo ) {
						playable.setCurrentTime( currentTime );
						if ( playable.getDuration() == 0 ) {
							playable.setDuration( totalTime );
						}
						playable.setState( state );

						lastVideo = (Video) playable;
						helper.updateTimeline( lastVideo );

						continue;
					}
				} else {
					if ( lastVideo != null ) {
						logger.fine( "There was a video playing last time" );
						playable = getNext();
						if ( playable != null ) {
							helper.play( 0, null );
						}
						lastVideo.setState( "stopped" );
						helper.updateTimeline( lastVideo );
						if ( playable != null && playable.getType() == Video.type ) {
							lastVideo = (Video) playable;
						}
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

						boolean sameTrack = playable.getPlayFile().equals( fullPath );

						if ( lastTrack != null && !sameTrack ) {
							logger.fine( "It's a different track than last time" );
							lastTrack.setState( "stopped" );
							helper.updateTimeline( lastTrack );
						}

						if ( sameTrack ) {
							if ( state.equals( "play" ) ) {
								state = "playing";
							} else if ( state.equals( "pause" ) ) {
								state = "paused";
							}

							playable.setCurrentTime( currentTime );
							if ( playable.getDuration() == 0 ) {
								playable.setDuration( totalTime );
							}

							playable.setState( state );
							lastTrack = (Track) playable;
							helper.updateTimeline( lastTrack );

							continue;
						}
					} else {
						if ( lastTrack != null ) {
							logger.fine( "No track this time, but there was a track last time" );
							playable = getNext();
							if ( playable != null ) {
								helper.play( 0, null );
							}
							lastTrack.setState( "stopped" );
							helper.updateTimeline( lastTrack );
							if ( playable != null && playable.getType() == Track.type ) {
								lastTrack = (Track) playable;
							}
							lastTrack = (Track) playable;
						}
					}
				}
			} catch ( Exception ex ) {
				ExceptionLogger.log( logger, ex );
			}
		}
		logger.info( "NowPlayingMonitor ending" );
	}
}
