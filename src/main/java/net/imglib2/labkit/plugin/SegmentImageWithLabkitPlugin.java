
package net.imglib2.labkit.plugin;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.trainable_segmentation.classification.Segmenter;
import net.imglib2.trainable_segmentation.gson.GsonUtils;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * @author Robert Haase
 * @author Matthias Arzt
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Segmentation > Labkit > Macro Recordable > Segment Image with Labkit")
public class SegmentImageWithLabkitPlugin implements Command, Cancelable {

	@Parameter
	private Context context;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private Dataset input;

	@Parameter
	private File segmenter_file;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(required = false)
	private Boolean use_gpu = false;

	@Override
	public void run() {
		Segmenter segmenter = Segmenter.fromJson(context, GsonUtils.read(
			segmenter_file.getAbsolutePath()));
		segmenter.setUseGpu(use_gpu);
		output = datasetService.create(segmenter.segment(input));
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void cancel(String reason) {

	}

	@Override
	public String getCancelReason() {
		return null;
	}
}
