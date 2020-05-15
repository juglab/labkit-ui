
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = SegmentationPlugin.class)
public class PixelClassificationPlugin implements SegmentationPlugin {

	@Parameter
	Context context;

	@Override
	public String getTitle() {
		return "Labkit Pixel Classification";
	}

	@Override
	public Segmenter createSegmenter(ImgPlus<?> image) {
		return new TrainableSegmentationSegmenter(context, image);
	}
}
