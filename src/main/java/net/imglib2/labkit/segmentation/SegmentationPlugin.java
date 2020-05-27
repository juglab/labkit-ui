
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import org.scijava.plugin.SciJavaPlugin;

public interface SegmentationPlugin extends SciJavaPlugin {

	String getTitle();

	Segmenter createSegmenter(ImgPlus<?> image);
}
