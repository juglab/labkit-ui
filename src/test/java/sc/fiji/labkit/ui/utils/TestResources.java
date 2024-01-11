package sc.fiji.labkit.ui.utils;

import sc.fiji.labkit.ui.plugin.SegmentImageWithLabkitPluginTest;

public class TestResources {

	public static String fullPath(String name) {
		return TestResources.class.getResource(name).getFile();
	}
}
