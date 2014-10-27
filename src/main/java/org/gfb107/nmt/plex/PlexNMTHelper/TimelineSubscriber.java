package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class TimelineSubscriber {
	private static Logger logger = Logger.getLogger( TimelineSubscriber.class.getName() );
	private String clientId;
	private String clientName;
	private String commandId;
	private String address;
	private int port;
	private String postUrl;
	private PlexServer server;
	private CloseableHttpClient client;

	public TimelineSubscriber( String commandId, String address, int port, PlexServer server ) {
		this.commandId = commandId;
		this.address = address;
		this.port = port;
		this.server = server;

		postUrl = "http://" + address + ':' + port + "/:/timeline";
	}

	public void setHttpClient( CloseableHttpClient client ) {
		this.client = client;
	}

	public void setClient( String clientId, String clientName ) {
		this.clientId = clientId;
		this.clientName = clientName;
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId( String commandId ) {
		this.commandId = commandId;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getPostUrl() {
		return postUrl;
	}

	public Element updateTimeline( Playable playable ) throws ValidityException, IllegalStateException, ClientProtocolException, ParsingException,
			IOException {
		if ( playable.getType() == Video.type ) {
			return updateTimeline( (Video) playable, playable.getState() );
		} else {
			return updateTimeline( (Track) playable, playable.getState() );
		}
	}

	public Element updateTimeline( Track audio, String state ) throws ValidityException, IllegalStateException, ClientProtocolException,
			ParsingException, IOException {
		Element audioElement = generateTimeline( audio, state );

		Element photoElement = generateEmptyTimeline( "photo" );

		Element videoElement = generateEmptyTimeline( "video" );

		return updateTimeline( audioElement, photoElement, videoElement );
	}

	public Element updateTimeline( Video video, String state ) throws ValidityException, IllegalStateException, ClientProtocolException,
			ParsingException, IOException {
		Element audioElement = generateEmptyTimeline( "music" );

		Element photoElement = generateEmptyTimeline( "photo" );

		Element videoElement = state.equals( "stopped" ) ? generateEmptyTimeline( "video" ) : generateTimeline( video, state );

		return updateTimeline( audioElement, photoElement, videoElement );
	}

	private Element updateTimeline( Element audioElement, Element photoElement, Element videoElement ) throws ClientProtocolException, IOException,
			ValidityException, IllegalStateException, ParsingException {

		Document container = generateTimelineContainer( audioElement, photoElement, videoElement );

		logger.finer( "Posting to " + postUrl );

		HttpPost post = new HttpPost( postUrl );
		post.addHeader( "Host", address + ":" + port );
		post.addHeader( "Content-Range", "bytes 0-/-1" );
		post.addHeader( "X-Plex-Client-Identifier", clientId );
		post.addHeader( "X-Plex-Device", "stb" );
		post.addHeader( "X-Plex-Device-Name", clientName );
		post.addHeader( "X-Plex-Provides", "player" );

		String xml = container.toXML();
		logger.finer( "Sending " + xml );

		post.setEntity( new StringEntity( xml ) );

		CloseableHttpResponse httpResponse = client.execute( post );
		HttpEntity entity = httpResponse.getEntity();

		Element response = null;

		if ( entity != null && entity.getContentType() != null && entity.getContentType().getValue().equals( "application/xml" ) ) {
			response = new Builder().build( entity.getContent() ).getRootElement();
		} else {
			StatusLine statusLine = httpResponse.getStatusLine();
			response = new Element( "Response" );
			response.addAttribute( new Attribute( "code", Integer.toString( statusLine.getStatusCode() ) ) );
			response.addAttribute( new Attribute( "status", statusLine.getReasonPhrase() ) );
			EntityUtils.consume( entity );
		}

		if ( logger.isLoggable( Level.FINE ) ) {
			logger.fine( "Response was " + response.toXML() );
		}
		return response;
	}

	public Element generateTimeline( Video video ) {
		return generateTimeline( video, video.getState() );
	}

	public Element generateTimeline( Video video, String state ) {
		Element timeline = new Element( "Timeline" );
		// timeline.addAttribute( new Attribute( "address", server.getAddress()
		// ) );
		timeline.addAttribute( new Attribute( "address", address ) );
		// timeline.addAttribute( new Attribute( "audioStreamID",
		// Integer.toString( video.getStream( audioStreamIndex ) ) ) );
		timeline.addAttribute( new Attribute( "containerKey", video.getContainerKey() ) );
		timeline.addAttribute( new Attribute( "controllable", "playPause,stop,seekTo,stepBack,stepForward" ) );
		timeline.addAttribute( new Attribute( "duration", Integer.toString( video.getDuration() ) ) );
		timeline.addAttribute( new Attribute( "guid", video.getGuid() ) );
		timeline.addAttribute( new Attribute( "key", video.getKey() ) );
		timeline.addAttribute( new Attribute( "location", video.getLocation() ) );
		timeline.addAttribute( new Attribute( "machineIdentifier", clientId ) );
		timeline.addAttribute( new Attribute( "mute", "0" ) );
		// timeline.addAttribute( new Attribute( "port", Integer.toString(
		// server.getPort() ) ) );
		timeline.addAttribute( new Attribute( "port", Integer.toString( port ) ) );
		timeline.addAttribute( new Attribute( "protocol", "http" ) );
		timeline.addAttribute( new Attribute( "ratingKey", video.getRatingKey() ) );
		timeline.addAttribute( new Attribute( "repeat", "0" ) );
		timeline.addAttribute( new Attribute( "seekRange", "0-" + Integer.toString( video.getDuration() ) ) );
		timeline.addAttribute( new Attribute( "shuffle", "0" ) );
		timeline.addAttribute( new Attribute( "state", state ) );
		timeline.addAttribute( new Attribute( "subtitleStreamID", "-1" ) );
		timeline.addAttribute( new Attribute( "time", Integer.toString( video.getCurrentTime() ) ) );
		timeline.addAttribute( new Attribute( "type", video.getType() ) );
		timeline.addAttribute( new Attribute( "volume", "100" ) );

		return timeline;
	}

	public Element generateTimeline( Track audio ) {
		return generateTimeline( audio, audio.getState() );
	}

	public Element generateTimeline( Track audio, String state ) {
		Element timeline = new Element( "Timeline" );
		timeline.addAttribute( new Attribute( "address", server.getAddress() ) );
		timeline.addAttribute( new Attribute( "containerKey", audio.getContainerKey() ) );
		timeline.addAttribute( new Attribute( "controllable", "playPause,stop,skipPrevious,skipNext,seekTo,repeat" ) );
		timeline.addAttribute( new Attribute( "duration", Integer.toString( audio.getDuration() ) ) );
		timeline.addAttribute( new Attribute( "key", audio.getKey() ) );
		timeline.addAttribute( new Attribute( "location", audio.getLocation() ) );
		timeline.addAttribute( new Attribute( "machineIdentifier", clientId ) );
		timeline.addAttribute( new Attribute( "mute", "0" ) );
		timeline.addAttribute( new Attribute( "port", Integer.toString( server.getPort() ) ) );
		timeline.addAttribute( new Attribute( "protocol", "http" ) );
		timeline.addAttribute( new Attribute( "ratingKey", audio.getRatingKey() ) );
		timeline.addAttribute( new Attribute( "repeat", "0" ) );
		timeline.addAttribute( new Attribute( "seekRange", "0-" + Integer.toString( audio.getDuration() ) ) );
		timeline.addAttribute( new Attribute( "shuffle", "0" ) );
		timeline.addAttribute( new Attribute( "state", state ) );
		timeline.addAttribute( new Attribute( "time", Integer.toString( audio.getCurrentTime() ) ) );
		timeline.addAttribute( new Attribute( "type", audio.getType() ) );
		timeline.addAttribute( new Attribute( "volume", "100" ) );

		if ( logger.isLoggable( Level.FINER ) ) {
			logger.finer( timeline.toXML() );
		}

		return timeline;
	}

	public Element generateEmptyTimeline( String type ) {
		Element timeline = new Element( "Timeline" );
		timeline.addAttribute( new Attribute( "location", "navigation" ) );
		// timeline.addAttribute( new Attribute( "seekRange", "0-0" ) );
		timeline.addAttribute( new Attribute( "state", "stopped" ) );
		timeline.addAttribute( new Attribute( "time", "0" ) );
		timeline.addAttribute( new Attribute( "type", type ) );

		return timeline;
	}

	public Document generateTimelineContainer( Element musicTimeline, Element photoTimeline, Element videoTimeline ) {
		Element container = new Element( "MediaContainer" );

		container.addAttribute( new Attribute( "commandID", commandId ) );
		String location = "fullScreenVideo";
		if ( musicTimeline.getAttributeValue( "location" ).equals( "navigation" )
				&& videoTimeline.getAttributeValue( "location" ).equals( "navigation" ) ) {
			location = "navigation";
		}
		container.addAttribute( new Attribute( "location", location ) );

		container.appendChild( musicTimeline );
		container.appendChild( photoTimeline );
		container.appendChild( videoTimeline );

		return new Document( container );
	}

}
