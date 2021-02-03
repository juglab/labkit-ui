
package net.imglib2.labkit.denoiseg;

import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import org.scijava.plugin.Plugin;

@Plugin(type = SegmentationPlugin.class)
public class DenoiSegSegmenterPlugin implements SegmentationPlugin {

	@Override
	public String getTitle() {
		return "DenoiSeg";
	}

	@Override
	public Segmenter createSegmenter() {
		return new DenoiSegSegmenter();
	}
}
