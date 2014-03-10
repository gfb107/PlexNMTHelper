package org.gfb107.nmt.plex.PlexNMTHelper;

import java.util.logging.Logger;

public class ExceptionLogger {
	public static void log( Logger logger, Throwable exception ) {
		logger.severe( exception.getClass().getName() + ": " + exception.getMessage() );
		logStackTrace( logger, exception );
		Throwable cause = exception.getCause();
		if ( cause != null ) {
			logger.severe( "caused by " + cause.getClass().getName() + ": " + cause.getMessage() );
			logStackTrace( logger, cause );
		}
	}

	private static void logStackTrace( Logger logger, Throwable exception ) {
		for ( StackTraceElement element : exception.getStackTrace() ) {
			logger.severe( "    " + element.toString() );
		}
	}
}
