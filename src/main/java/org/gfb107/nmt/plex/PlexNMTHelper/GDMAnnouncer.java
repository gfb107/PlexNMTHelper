package org.gfb107.nmt.plex.PlexNMTHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;

public class GDMAnnouncer implements Runnable {

	private static Logger logger = Logger.getLogger( GDMAnnouncer.class.getName() );
	private InetAddress gdmAddress = null;
	private static final int announcePort = 32412;
	private String announceMessage = null;
	private boolean stop = false;

	public GDMAnnouncer( String name, String clientId, int port ) {
		StringBuilder sb = new StringBuilder( "HELLO * HTTP/1.0\n" );
		sb.append( "Name: " + name + "\n" );
		sb.append( "Port: " + port + "\n" );
		sb.append( "Product: Networked Media Tank Plex Helper\n" );
		sb.append( "Content-Type: plex/media-player\n" );
		sb.append( "Protocol: plex\n" );
		sb.append( "Protocol-Version: 1\n" );
		sb.append( "Protocol-Capabilities: timeline,playback,navigation\n" );
		sb.append( "Version: 0.1\n" );
		sb.append( "Resource-Identifier: " + clientId + "\n" );
		sb.append( "Device-Class: stb\n" );
		sb.append( "\n" );

		announceMessage = sb.toString();
	}

	public void setStop( boolean stop ) {
		this.stop = stop;
	}

	public void run() {
		logger.fine( "GDMAnnouncer running" );

		MulticastSocket announceSocket = null;
		try {
			gdmAddress = InetAddress.getByName( "239.0.0.250" );

			announceSocket = new MulticastSocket( announcePort );

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
				while ( reader.readLine() != null ) {
					;
				}

				if ( !line.contains( "M-SEARCH * HTTP/1." ) ) {
					continue;
				}

				logger.finer( announceMessage );

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
