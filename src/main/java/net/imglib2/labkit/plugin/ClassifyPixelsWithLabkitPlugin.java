
package net.imglib2.labkit.plugin;

import jdk.nashorn.internal.parser.JSONParser;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.trainable_segmentation.classification.Segmenter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * @author Robert Haase
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Classify pixels with Labkit")
public class ClassifyPixelsWithLabkitPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private Dataset dataset;

	@Parameter
	private File modelFile;

	@Override
	public void run() {
		if (true) return;
		DatasetInputImage input = new DatasetInputImage(dataset);

		String model = modelFile.getAbsolutePath();

		//Segmenter segmenter = Segmenter.fromJson(context, new JSONParser(model, null, false));
		DefaultSegmentationModel defaultSegmentationModel = new DefaultSegmentationModel(input, context);

		List<RandomAccessibleInterval<IntType>> segmentations = defaultSegmentationModel.getSegmentations(new IntType());

		for (RandomAccessibleInterval<IntType> whatever : segmentations) {
			ImageJFunctions.show(whatever, "Labkit segmentation " + dataset.getName() + " " + modelFile.getAbsolutePath());
		}
	}
}
