
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
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
		boolean isTimelapse = ImgPlusViewsOld.hasAxis(image, Axes.TIME);
		if (isTimelapse) {
			int d = image.dimensionIndex(Axes.TIME);
			long min = image.min(d);
			ImgPlus<?> firstFrame = ImgPlusViews.hyperSlice((ImgPlus) image, d, min);
			TrainableSegmentationSegmenter segmenter = new TrainableSegmentationSegmenter(context,
				firstFrame);
			return new TimeSeriesSegmenter(segmenter);
		}
		else {
			return new TrainableSegmentationSegmenter(context, image);
		}
	}

	public static SegmentationPlugin create() {
		Context context = SingletonContext.getInstance();
		PixelClassificationPlugin plugin = new PixelClassificationPlugin();
		context.inject(plugin);
		return plugin;
	}
}
