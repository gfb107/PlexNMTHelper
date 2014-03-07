package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class NetworkedMediaTank {
	private static Logger logger = Logger.getLogger( NetworkedMediaTank.class.getName() );
	private String address;
	private String name;

	public static final String productName = "Networked Media Tank";

	private String prefix;

	private String macAddress;

	private CloseableHttpClient client = HttpClients.createDefault();

	public NetworkedMediaTank( String address, String name ) {
		this.address = address;
		this.name = name;

		prefix = "http://" + address + ":8008/";
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public String getMacAddress() throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException,
			InterruptedException {
		if ( macAddress == null ) {
			Element data = sendCommand( "system", "get_mac_address" );
			macAddress = data.getFirstChildElement( "response" ).getFirstChildElement( "macAddress" ).getValue();
		}
		return macAddress;
	}

	public Element sendCommand( String module, String... args ) throws ClientProtocolException, IOException, ValidityException,
			IllegalStateException, ParsingException, InterruptedException {
		StringBuilder sb = new StringBuilder( prefix );
		sb.append( module );
		for ( int i = 0; i < args.length; i++ ) {
			if ( i == 0 ) {
				sb.append( '?' );
			} else {
				sb.append( '&' );
			}
			sb.append( "arg" ).append( i ).append( '=' ).append( args[i] );
		}

		String url = sb.toString();
		logger.finer( "Getting " + url );

		Builder builder = new Builder();
		Element response;
		while ( true ) {

			try {
				response = builder.build( client.execute( new HttpGet( url ) ).getEntity().getContent() ).getRootElement();
				if ( logger.isLoggable( Level.FINER ) ) {
					logger.finer( "Response was " + response.toXML() );
				}
				break;
			} catch ( SocketTimeoutException ex ) {
				logger.warning( "Request timed out, will retry" );
				Thread.sleep( 1000 );
			}
		}

		Thread.sleep( 100 );
		return response;
	}

	public String sendKey( String key, String module ) throws ClientProtocolException, IOException, ValidityException, IllegalStateException,
			ParsingException, InterruptedException {
		if ( key != null ) {
			sendCommand( "system", "send_key", key, module );
		}

		return null;
	}

	private String fix( String text ) throws UnsupportedEncodingException {
		return URLEncoder.encode( text, "utf-8" ).replace( "+", "%20" );
	}

	public void play( Video video, int time ) throws ClientProtocolException, ValidityException, IllegalStateException, UnsupportedEncodingException,
			IOException, ParsingException, InterruptedException {
		sendCommand( "playback", "start_vod", fix( video.getTitle() ), fix( video.getFile() ).replace( "%2F", "/" ), "show",
				Integer.toString( time / 1000 ) );
	}

	public void insertInQueue( Track track ) throws ClientProtocolException, ValidityException, IllegalStateException, UnsupportedEncodingException,
			IOException, ParsingException, InterruptedException {
		sendCommand( "playback", "insert_aod_queue", fix( track.getTitle() ), track.getFile(), "show" );
	}

	public String getQueueSongTitle( String fullPath ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, InterruptedException {
		Element container = sendCommand( "playback", "list_aod_queue_info" );

		int returnValue = Integer.parseInt( container.getFirstChildElement( "returnValue" ).getValue() );

		if ( returnValue == 0 ) {
			Elements queues = container.getFirstChildElement( "response" ).getChildElements();
			for ( int i = 0; i < queues.size(); ++i ) {
				Element queue = queues.get( i );
				if ( queue.getFirstChildElement( "fullpath" ).getValue().equals( fullPath ) ) {
					return queue.getFirstChildElement( "title" ).getValue();
				}
			}
		}

		return null;
	}

	public String getConvertedPath( String path ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, InterruptedException {
		Element result = sendCommand( "file_operation", "list_user_storage_file", path, "0", "0", "false", "false", "false", "" );
		if ( result.getFirstChildElement( "returnValue" ).getValue().equals( "0" ) ) {
			return result.getFirstChildElement( "response" ).getFirstChildElement( "convertPath" ).getValue();
		}

		return null;
	}

}
