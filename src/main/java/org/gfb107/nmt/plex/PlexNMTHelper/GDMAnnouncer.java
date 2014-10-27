package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class GDMAnnouncer implements Runnable {

	private static Logger logger = Logger.getLogger( GDMAnnouncer.class.getName() );
	private InetAddress gdmAddress = null;
	private InetAddress myAddress = null;
	private static final int announcePort = 32412;
	private String announceMessage = null;
	private boolean stop = false;

	public GDMAnnouncer( String name, String clientId, InetAddress address, int port ) {
		myAddress = address;
		StringBuilder sb = new StringBuilder( "HTTP/1.0 200 OK\r\n" );
		sb.append( "Content-Type: plex/media-player\r\n" );
		sb.append( "Resource-Identifier: " + clientId + "\r\n" );
		// sb.append( "Machine-Identifier: " + clientId + "\r\n" );

		sb.append( "Device-Class: stb\r\n" );
		sb.append( "Name: " + name + "\r\n" );
		sb.append( "Port: " + port + "\r\n" );

		// sb.append( "Host: " + address.getHostAddress() + "\n" );

		sb.append( "Product: " + NetworkedMediaTank.productName + "\r\n" );
		sb.append( "Protocol: plex\r\n" );
		sb.append( "Protocol-Capabilities: navigation,playback,timeline\r\n" );
		sb.append( "Protocol-Version: 1\r\n" );
		sb.append( "Version: 0.0.1\r\n" );

		sb.append( "\r\n" );

		announceMessage = sb.toString();
		logger.finer( announceMessage );
	}

	public void setStop( boolean stop ) {
		this.stop = stop;
	}

	public void run() {
		logger.fine( "GDMAnnouncer running" );

		MulticastSocket announceSocket = null;
		try {
			gdmAddress = InetAddress.getByName( "239.0.0.250" );
			SocketAddress socketAddress = new InetSocketAddress( myAddress, announcePort );
			announceSocket = new MulticastSocket( socketAddress );

			announceSocket.joinGroup( gdmAddress );
		} catch ( IOException e ) {
			ExceptionLogger.log( logger, e );
		}

		byte[] buf = new byte[1000];

		while ( !stop ) {
			try {
				DatagramPacket pollPacket = new DatagramPacket( buf, buf.length );
				announceSocket.receive( pollPacket );

				BufferedReader reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( pollPacket.getData() ) ) );

				String line = reader.readLine();
				logger.finer( line );
				while ( reader.readLine() != null ) {}

				if ( !line.contains( "M-SEARCH * HTTP/1." ) ) {
					continue;
				}

				DatagramPacket announcePacket = new DatagramPacket( announceMessage.getBytes(), announceMessage.length(), pollPacket.getAddress(),
						pollPacket.getPort() );

				announceSocket.send( announcePacket );
			} catch ( Exception ex ) {
				ExceptionLogger.log( logger, ex );
			}
		}

		announceSocket.close();
	}
}
