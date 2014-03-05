package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class GDMDiscovery {

	private static final String broadcastAddress = "239.0.0.250";
	private static final int discoveryPort = 32414;
	private static final String discoveryMessage = "M-SEARCH * HTTP/1.1\r\n\r\n";

	private int port = 0;

	public GDMDiscovery( int port ) {}

	public PlexServer discover() throws ValidityException, IllegalStateException, ParsingException {

		DatagramSocket discoverySocket = null;
		try {
			InetAddress gdmAddress = InetAddress.getByName( broadcastAddress );

			discoverySocket = new DatagramSocket( port );

			DatagramPacket discoveryPacket = new DatagramPacket( discoveryMessage.getBytes(), discoveryMessage.length(), gdmAddress, discoveryPort );

			discoverySocket.send( discoveryPacket );

			byte[] buf = new byte[4 * 1024];
			DatagramPacket responsePacket = new DatagramPacket( buf, buf.length );

			discoverySocket.receive( responsePacket );

			String serverAddress = responsePacket.getAddress().getHostAddress();
			int serverPort = 0;
			String serverName = null;

			BufferedReader rdr = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( responsePacket.getData() ) ) );
			String line = rdr.readLine();

			if ( line.equals( "HTTP/1.0 200 OK" ) ) {
				while ( (line = rdr.readLine()) != null ) {
					String[] parts = line.split( ":" );
					if ( parts.length != 2 ) {
						continue;
					}
					String name = parts[0];
					String value = parts[1].substring( 1 );
					if ( name.equals( "Port" ) ) {
						serverPort = Integer.parseInt( value );
					} else if ( name.equals( "Name" ) ) {
						serverName = value;
					}
				}

				System.out.println( "Found PLEX server '" + serverName + "' at " + serverAddress + ':' + serverPort );

				return new PlexServer( serverAddress, serverPort, serverName );
			}

			discoverySocket.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

}
