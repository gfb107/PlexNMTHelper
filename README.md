#PlexNMTHelper

A companion/helper application to enable flinging PLEX media to and remote control of a Networked Media Tank (200-series or later)

PlexNMTHelper is a Java application that you can run anywhere on your home network (I run it on my PLEX Server) that enables remote control of a Networed Media Tank (NMT).

It can be controlled using
  Plex Home Theater (Windows, OSX, Linux)
  Plex Mobile (iOS, Android, Windows 8 Phone, Windows 8)
  Plex/Web
  
###Demos
* [Google Nexus 7 and Plex for PlexPass for Android](https://www.youtube.com/watch?v=_WQk7E0bWyo)
* [Applie iPhone 4s and Plex for iOS](https://www.youtube.com/watch?v=OuxCLOtRjL4)

###Prerequisites

1. A CloudMedia Popcorn Hour Networked Media Tank, 200 series or later
2. A Java 6 or later runtime environment installed, with the bin folder in the path.

###Installation

1. Download the [PlexNMTHelper.zip](https://github.com/gfb107/PlexNMTHelper/releases/download/v1.0.10/PlexNMTHelper.zip)
2. Unzip to the installation folder of your choice (e.g. C:\Program Files\PlexNMTHelper )
3. Copy *PlexNMTHelper.properties* and *config.xml* from the samples sub-folder to the installation folder.
4. Modify *PlexNMTHelper.properties* and *config.xml* for your configuration
5. Run *PlexNMTHelper.cmd*

###History

Version 1.0.10
	Fix to TV Episodes

Version 1.0.9
	Fixes for Plex/Web client
	Improved PlayQueue/Playlist support
	Removed Cache
	
Version 1.0.8
	Basic fix for PlayQueues, does not support multiple items in the queue
	
Version 1.0.7
	Don't use list_user_storage_file to convert file paths that are already in NMT format
	
Version 1.0.6
	Add config.xml logging
	
Version 1.0.5
	Improvements for iOS support
	Fix config.xml issues

Version 1.0.4
	Remove discoveryPort configuration parameter. Only one port is needed.
	Clean up exception logging.
	If there is no replacement defined in config.xml for videos files that start with \\ or //, automatically create one 
	Remove HTTP streaming warning when no matching replacement found for a video file.
	
Version 1.0.3
	Fix for Plex not providing a duration for some media

Version 1.0.2
	Add support for multiple library sections of the same type
	Add stepBack and stepForward support
	
Version 1.0.1
	Logging improvements
	Tweaks for HTTP streaming  

Version 1.0
	Initial release
	
