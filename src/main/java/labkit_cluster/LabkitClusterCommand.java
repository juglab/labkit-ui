
package labkit_cluster;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class)
public class LabkitClusterCommand implements Command {

	@Parameter
	private String classifier;

	@Parameter
	private String input;

	@Parameter
	private Interval interval;

	@Parameter
	// TODO rename because it's not an output an therefore confusiong
	private String output;

	@Parameter
	private Context context;

	public LabkitClusterCommand() {}

	@Override
	public void run() {
		System.out.println(interval);
		SpimDataInputImage data = new SpimDataInputImage(input, 0);
		RandomAccessibleInterval<? extends NumericType<?>> image = data
			.imageForSegmentation();
		TrainableSegmentationSegmenter segmenter =
			new TrainableSegmentationSegmenter(context, data);
		segmenter.openModel(classifier);
		IntervalView<UnsignedByteType> segmentation = createImg(interval);
		segmenter.segment(image, segmentation);
		MyN5.writeBlock(output, segmentation);
		System.out.println(interval);
	}

	private IntervalView<UnsignedByteType> createImg(Interval interval) {
		long[] offset = Intervals.minAsLongArray(interval);
		final long[] dim = Intervals.dimensionsAsLongArray(interval);
		return Views.translate(ArrayImgs.unsignedBytes(dim), offset);
	}

}
