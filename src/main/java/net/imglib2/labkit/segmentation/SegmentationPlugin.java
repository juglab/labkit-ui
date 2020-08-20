
package net.imglib2.labkit.segmentation;

import org.scijava.plugin.SciJavaPlugin;

public interface SegmentationPlugin extends SciJavaPlugin {

	String getTitle();

	Segmenter createSegmenter();
}
