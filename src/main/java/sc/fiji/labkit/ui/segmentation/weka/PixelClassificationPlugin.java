
package sc.fiji.labkit.ui.segmentation.weka;

import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
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
