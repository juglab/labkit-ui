
package net.imglib2.labkit.plugin;

import jdk.nashorn.internal.parser.JSONParser;
import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.trainable_segmentation.classification.Segmenter;
import net.imglib2.trainable_segmentation.gson.GsonUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
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
	private File model_file;

	@Parameter
	private Boolean use_gpu = false;

	@Override
	public void run() {
		RandomAccessibleInterval image = dataset;
		Segmenter segmenter = Segmenter.fromJson(new Context(), GsonUtils.read(model_file.getAbsolutePath()));
		segmenter.setUseGpu(use_gpu);
		RandomAccessibleInterval<UnsignedByteType> result = segmenter.segment(image);
		ImageJFunctions.show(result, "Labkit segmentation " + dataset.getName() + " " + model_file);
	}
}
