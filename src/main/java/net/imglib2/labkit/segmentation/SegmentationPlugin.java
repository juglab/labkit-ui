
package net.imglib2.labkit.segmentation;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Interface that must be implemented by segmentation algorithm plugins.
 */
public interface SegmentationPlugin extends SciJavaPlugin {

	String getTitle();

	Segmenter createSegmenter();
}
