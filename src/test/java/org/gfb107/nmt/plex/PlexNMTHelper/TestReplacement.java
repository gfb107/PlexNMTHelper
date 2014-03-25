package org.gfb107.nmt.plex.PlexNMTHelper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestReplacement {

	@Test
	public void test() {
		Replacement replacement = new Replacement( "//Mini/Mounts/", "smb://Mini/Mounts" );
		String path = "//Mini/Mounts/Movies/17 Again.mkv";
		assert (replacement.matches( path ));
		assertEquals( "smb://Mini/Mounts/Movies/17 again.mkv", replacement.convert( path ) );
	}

}
