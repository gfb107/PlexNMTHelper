package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class PlexServer {
	private static Logger logger = Logger.getLogger( PlexServer.class.getName() );
	private String name;
	private String address;
	private int port;

	private String clientId;
	private String clientName;

	private CloseableHttpClient client;

	private Element successResponse = null;

	public PlexServer( String address, int port, String name ) {
		this.address = address;
		this.port = port;
		this.name = name;

		successResponse = new Element( "Response" );
		successResponse.addAttribute( new Attribute( "code", "200" ) );
		successResponse.addAttribute( new Attribute( "status", "OK" ) );
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public void setClient( CloseableHttpClient client ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
		this.client = client;
	}

	public void setClientName( String clientName ) {
		this.clientName = clientName;
	}

	public void setClientId( String clientId ) {
		this.clientId = clientId;
	}

	private URIBuilder getBuilder() {
		return new URIBuilder().setScheme( "http" ).setHost( address ).setPort( port );
	}

	public Element sendCommand( URI uri ) throws ClientProtocolException, IOException, ValidityException, IllegalStateException, ParsingException {
		logger.fine( "Getting " + uri );
		HttpGet get = new HttpGet( uri );
		get.addHeader( "X-Plex-Client-Identifier", clientId );
		get.addHeader( "X-Plex-Device", "stb" );
		get.addHeader( "X-Plex-Device-Name", clientName );
		get.addHeader( "X-plex-Model", "Linux" );
		get.addHeader( "X-Plex-Provides", "player" );
		get.addHeader( "X-Plex-Version", "0.1" );

		CloseableHttpResponse httpResponse = client.execute( get );
		HttpEntity entity = httpResponse.getEntity();

		Element response = successResponse;

		if ( entity != null && entity.getContentType().getValue().split( ";" )[0].equals( "text/xml" ) ) {
			response = new Builder().build( entity.getContent() ).getRootElement();
		} else {
			StatusLine statusLine = httpResponse.getStatusLine();
			response = new Element( "Response" );
			response.addAttribute( new Attribute( "code", Integer.toString( statusLine.getStatusCode() ) ) );
			response.addAttribute( new Attribute( "status", statusLine.getReasonPhrase() ) );
			EntityUtils.consume( entity );
		}

		logger.finer( "Response was " + response.toXML() );
		return response;
	}

	public PlayQueue getPlayQueue( String fullContainerKey ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, URISyntaxException {
		String containerKey = fullContainerKey.substring( 0, fullContainerKey.indexOf( '?' ) );
		Element element = sendCommand( getBuilder().setPath( containerKey ).build() );

		PlayQueue queue = new PlayQueue( containerKey, Integer.parseInt( element.getAttributeValue( "playQueueSelectedItemOffset" ) ) );
		Elements tracks = element.getChildElements();
		for ( int t = 0; t < tracks.size(); ++t ) {
			Element trackElement = tracks.get( t );
			String type = trackElement.getAttributeValue( "type" );
			if ( type.equals( "movie" ) ) {
				queue.add( getVideo( containerKey, trackElement ) );
			} else if ( type.equals( "track" ) ) {
				queue.add( getTrack( containerKey, trackElement ) );
			}
		}
		return queue;
	}

	public Element updateTimeline( Video video ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, URISyntaxException {
		URIBuilder builder = getBuilder().setPath( "/:/timeline" ).addParameter( "containerKey", video.getContainerKey() )
				.addParameter( "duration", Integer.toString( video.getDuration() ) ).addParameter( "guid", video.getGuid() )
				.addParameter( "key", video.getKey() ).addParameter( "ratingKey", video.getRatingKey() ).addParameter( "state", video.getState() )
				.addParameter( "time", Integer.toString( video.getCurrentTime() ) );

		return sendCommand( builder.build() );
	}

	public Element updateTimeline( Track audio ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, URISyntaxException {
		URIBuilder builder = getBuilder().setPath( "/:/timeline" ).addParameter( "containerKey", audio.getContainerKey() )

		.addParameter( "duration", Integer.toString( audio.getDuration() ) ).addParameter( "key", audio.getKey() )
				.addParameter( "ratingKey", audio.getRatingKey() ).addParameter( "state", audio.getState() )
				.addParameter( "time", Integer.toString( audio.getCurrentTime() ) );

		return sendCommand( builder.build() );
	}

	private String getPrefix() {
		return "http://" + address + ':' + port;
	}

	public Video getVideo( String containerKey, Element videoElement ) throws ClientProtocolException, ValidityException, IllegalStateException,
			IOException, ParsingException {
		String key = videoElement.getAttributeValue( "key" );
		String ratingKey = videoElement.getAttributeValue( "ratingKey" );
		String title = videoElement.getAttributeValue( "title" );
		String guid = videoElement.getAttributeValue( "guid" );
		int duration = 0;
		String temp = videoElement.getAttributeValue( "duration" );
		if ( temp != null ) {
			duration = Integer.parseInt( temp );
		}
		Element media = videoElement.getFirstChildElement( "Media" );
		Element part = media.getFirstChildElement( "Part" );
		String file = part.getAttributeValue( "file" );
		String httpFile = getPrefix() + part.getAttributeValue( "key" );

		return new Video( containerKey, key, ratingKey, title, guid, duration, file, httpFile );
	}

	public Track getTrack( String containerKey, Element trackElement ) {
		String ratingKey = trackElement.getAttributeValue( "ratingKey" );
		String key = trackElement.getAttributeValue( "key" );
		String title = trackElement.getAttributeValue( "title" );
		int duration = 0;
		String temp = trackElement.getAttributeValue( "duration" );
		if ( temp != null ) {
			duration = Integer.parseInt( temp );
		}
		Element part = trackElement.getFirstChildElement( "Media" ).getFirstChildElement( "Part" );
		String file = part.getAttributeValue( "key" );

		Track track = new Track( containerKey, key, ratingKey, title, file, duration );
		track.setPlayFile( getPrefix() + file );
		return track;
	}
}
