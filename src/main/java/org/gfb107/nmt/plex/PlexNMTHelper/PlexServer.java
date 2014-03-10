package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class PlexServer {
	private static Logger logger = Logger.getLogger( PlexServer.class.getName() );
	private String name;
	private String address;
	private int port;

	private String clientId;
	private String clientName;

	private String prefix;

	private CloseableHttpClient client;

	private Element successResponse = null;

	public PlexServer( String address, int port, String name ) {
		this.address = address;
		this.port = port;
		this.name = name;

		prefix = "http://" + address + ':' + port;

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

	public List< String > getLibrarySections( String... types ) throws ClientProtocolException, ValidityException, IllegalStateException,
			IOException, ParsingException {
		Arrays.sort( types );
		Element container = sendCommand( "/library/sections" );
		Elements directories = container.getChildElements( "Directory" );
		List< String > sections = new ArrayList< String >( directories.size() );
		for ( int i = 0; i < directories.size(); ++i ) {
			Element directory = directories.get( i );
			if ( Arrays.binarySearch( types, directory.getAttributeValue( "type" ) ) >= 0 )
				sections.add( directory.getAttributeValue( "key" ) );
		}
		return sections;
	}

	public void setClientName( String clientName ) {
		this.clientName = clientName;
	}

	public void setClientId( String clientId ) {
		this.clientId = clientId;
	}

	public Element sendCommand( String path ) throws ClientProtocolException, IOException, ValidityException, IllegalStateException, ParsingException {
		logger.fine( "Getting " + path );
		HttpGet get = new HttpGet( prefix + path );
		get.addHeader( "X-Plex-Client-Identifier", clientId );
		get.addHeader( "X-Plex-Device", "stb" );
		get.addHeader( "X-Plex-Device-Name", clientName );
		get.addHeader( "X-Plex-Provides", "player" );

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

	public Element updateTimeline( Video video, String state ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
		StringBuilder sb = new StringBuilder( "/:/timeline?containerKey=" ).append( video.getContainerKey() );
		sb.append( "&duration=" ).append( video.getDuration() );
		sb.append( "&guid=" + URLEncoder.encode( video.getGuid(), "utf-8" ) );
		sb.append( "&key=" ).append( video.getKey() );
		sb.append( "&ratingKey=" ).append( video.getRatingKey() );
		sb.append( "&state=" ).append( state );
		sb.append( "&time=" ).append( video.getCurrentTime() );

		return sendCommand( sb.toString() );
	}

	public Element updateTimeline( Track audio, String state ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
		StringBuilder sb = new StringBuilder( "/:/timeline?containerKey=" ).append( audio.getContainerKey() );
		sb.append( "&duration=" ).append( audio.getDuration() );
		sb.append( "&key=" ).append( audio.getKey() );
		sb.append( "&ratingKey=" ).append( audio.getRatingKey() );
		sb.append( "&state=" ).append( state );
		sb.append( "&time=" ).append( audio.getCurrentTime() );

		return sendCommand( sb.toString() );
	}

	public Video getVideo( Element videoElement ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
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
		String httpFile = prefix + part.getAttributeValue( "key" );

		return new Video( key, ratingKey, title, guid, duration, file, httpFile );
	}

	private Track getTrack( Element track ) {
		String ratingKey = track.getAttributeValue( "ratingKey" );
		String key = track.getAttributeValue( "key" );
		String title = track.getAttributeValue( "title" );
		String parentKey = track.getAttributeValue( "parentKey" );
		int duration = 0;
		String temp = track.getAttributeValue( "duration" );
		if ( temp != null ) {
			duration = Integer.parseInt( temp );
		}
		Element part = track.getFirstChildElement( "Media" ).getFirstChildElement( "Part" );
		String file = prefix + part.getAttributeValue( "key" );

		return new Track( parentKey, key, ratingKey, title, file, duration );
	}

	public Track[] getTracks( String containerKey ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
		Element container = sendCommand( containerKey );
		Elements tracks = container.getChildElements();

		Track[] list = new Track[tracks.size()];

		for ( int i = 0; i < list.length; ++i ) {
			list[i] = getTrack( tracks.get( i ) );
		}

		return list;
	}

	public void updateVideo( Video video ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException {
		Element container = sendCommand( video.getContainerKey() );
		video.setGuid( getVideo( container.getFirstChildElement( "Video" ) ).getGuid() );
	}

	public Video getVideo( String containerKey ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException {
		return getVideo( sendCommand( containerKey ).getFirstChildElement( "Video" ) );
	}

	public List< Track > getKnownTracks() throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException {
		ArrayList< Track > tracks = new ArrayList< Track >();
		List< String > sections = getLibrarySections( "artist" );
		for ( String section : sections ) {
			Element container = sendCommand( "/library/sections/" + section + "/allLeaves" );
			Elements trackElements = container.getChildElements();
			tracks.ensureCapacity( tracks.size() + trackElements.size() );
			for ( int i = 0; i < trackElements.size(); ++i ) {
				tracks.add( getTrack( trackElements.get( i ) ) );
			}
		}

		return tracks;
	}

	public List< Video > getKnownVideos() throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException {
		ArrayList< Video > videos = new ArrayList< Video >();
		List< String > sections = getLibrarySections( "movie", "show" );
		for ( String section : sections ) {
			Element container = sendCommand( "/library/sections/" + section + "/allLeaves" );
			Elements videoElements = container.getChildElements();
			videos.ensureCapacity( videos.size() + videoElements.size() );
			for ( int i = 0; i < videoElements.size(); ++i ) {
				videos.add( getVideo( videoElements.get( i ) ) );
			}
		}
		videos.trimToSize();

		return videos;
	}
}
