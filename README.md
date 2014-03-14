PlexNMTHelper
=============

A companion/helper application to enable flinging PLEX media to and remote control of a Networked Media Tank (200-series or later)

PlexNMTHelper is a Java application that you can run anywhere on your home network (I run it on my PLEX Server) that enables remote control of a Networed Media Tank (NMT).

It can be controlled using
  Plex Home Theater (Windows, OSX, Linux)
  Plex Mobile (iOS, Android, Windows 8 Phone, Windows 8)
  Plex/Web
  

History
=======
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
	
