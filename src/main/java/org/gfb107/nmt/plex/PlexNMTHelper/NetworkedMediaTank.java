package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class NetworkedMediaTank {
	private Logger logger = Logger.getLogger( NetworkedMediaTank.class.getName() );
	private String address;
	private String name;

	public static final String productName = "Plex NMT Helper";
	public static final String productVersion = "1.0";

	private String prefix;

	private String macAddress;

	private CloseableHttpClient client = HttpClients.createDefault();

	public NetworkedMediaTank( String address, String name ) {
		this.address = address;
		this.name = name;

		prefix = "http://" + address + ":8008/";
	}

	private URIBuilder getUriBuilder() {
		return new URIBuilder().setScheme( "http" ).setHost( address ).setPort( 8008 );
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public String getMacAddress() throws ClientProtocolException, ValidityException, IllegalStateException, IOException, ParsingException,
			InterruptedException, URISyntaxException {
		if ( macAddress == null ) {
			Element data = sendCommand( "system", "get_mac_address" );
			macAddress = data.getFirstChildElement( "response" ).getFirstChildElement( "macAddress" ).getValue();
		}
		return macAddress;
	}

	public Element sendCommand( String module, String... args ) throws ClientProtocolException, IOException, ValidityException,
			IllegalStateException, ParsingException, InterruptedException, URISyntaxException {
		URIBuilder uriBuilder = getUriBuilder();
		uriBuilder.setPath( "/" + module );
		for ( int i = 0; i < args.length; i++ ) {
			uriBuilder.setParameter( "arg" + i, args[i] );
		}

		String url = uriBuilder.build().toString().replace( "+", "%20" ).replace( "%2F", "/" );
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
			ParsingException, InterruptedException, URISyntaxException {
		if ( key != null ) {
			sendCommand( "system", "send_key", key, module );
		}

		return null;
	}

	private String fix( String text ) throws UnsupportedEncodingException {
		return text;
		// return URLEncoder.encode( text, "utf-8" ).replace( "+", "%20" );
	}

	public void play( Playable playable, int time ) throws ClientProtocolException, ValidityException, IllegalStateException,
			UnsupportedEncodingException, IOException, ParsingException, InterruptedException, URISyntaxException {

		if ( playable.getType() == Video.type ) {
			// playFile = fix( playFile ).replace( "%2F", "/" );
			sendCommand( "playback", "start_vod", playable.getTitle(), playable.getPlayFile(), "show", Integer.toString( time / 1000 ) );
		} else {
			sendCommand( "playback", "start_aod", playable.getTitle(), playable.getPlayFile(), "show" );
		}
	}

	public String getConvertedPath( String path ) throws ClientProtocolException, ValidityException, IllegalStateException, IOException,
			ParsingException, InterruptedException, URISyntaxException {
		if ( path.startsWith( "http://" ) || path.contains( "/opt/sybhttpd/localhost.drives/" ) ) {
			return path;
		}
		Element result = sendCommand( "file_operation", "list_user_storage_file", path, "0", "0", "false", "false", "false", "" );
		if ( result.getFirstChildElement( "returnValue" ).getValue().equals( "0" ) ) {
			return result.getFirstChildElement( "response" ).getFirstChildElement( "convertPath" ).getValue();
		}

		return null;
	}

}
