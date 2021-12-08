
package net.imglib2.labkit.segmentation.weka;

import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Plugin provides the "Labkit Pixel Classification".
 */
@Plugin(type = SegmentationPlugin.class)
public class PixelClassificationPlugin implements SegmentationPlugin {

	@Parameter
	Context context;

	@Override
	public String getTitle() {
		return "Labkit Pixel Classification";
	}

	@Override
	public Segmenter createSegmenter() {
		return new TrainableSegmentationSegmenter(context);
	}

	@Override
	public boolean canOpenFile(String filename) {
		try {
			new TrainableSegmentationSegmenter(context).openModel(filename);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static SegmentationPlugin create() {
		Context context = SingletonContext.getInstance();
		PixelClassificationPlugin plugin = new PixelClassificationPlugin();
		context.inject(plugin);
		return plugin;
	}
}
