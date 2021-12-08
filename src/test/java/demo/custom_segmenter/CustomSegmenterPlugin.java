
package demo.custom_segmenter;

import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import org.scijava.plugin.Plugin;

/**
 * A simple SciJava plugin that makes Labkit find the {@link CustomSegmenter}
 * class.
 */
@Plugin(type = SegmentationPlugin.class)
public class CustomSegmenterPlugin implements SegmentationPlugin {

	@Override
	public String getTitle() {
		return "Threshold";
	}

	@Override
	public Segmenter createSegmenter() {
		return new CustomSegmenter();
	}

	@Override
	public boolean canOpenFile(String filename) {
		return false;
	}
}
