
package demo.mats_1;

import net.imagej.ImgPlus;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import org.scijava.plugin.Plugin;

@Plugin(type = SegmentationPlugin.class)
public class BiisSegmentationPlugin implements SegmentationPlugin {

	@Override
	public String getTitle() {
		return "Bayes Inference";
	}

	@Override
	public Segmenter createSegmenter(ImgPlus<?> image) {
		return new BiisSegmenterAdapter(null, null);
	}
}
