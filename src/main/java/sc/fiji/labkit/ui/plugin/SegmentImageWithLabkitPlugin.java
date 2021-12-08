
package sc.fiji.labkit.ui.plugin;

import bdv.export.ProgressWriterConsole;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.models.DefaultCachedImageFactory;
import sc.fiji.labkit.ui.segmentation.SegmentationUtils;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;
import sc.fiji.labkit.ui.utils.ParallelUtils;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;
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
	menuPath = "Plugins > Labkit > Macro Recordable > Segment Image With Labkit")
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
		TrainableSegmentationSegmenter segmenter = new TrainableSegmentationSegmenter(context);
		segmenter.setUseGpu(use_gpu);
		segmenter.openModel(segmenter_file.getAbsolutePath());
		ImgPlus<?> imgPlus = new DatasetInputImage(input).imageForSegmentation();
		Img<ShortType> outputImg = useCache(imgPlus) ? calculateOnCachedImg(segmenter, imgPlus)
			: calculateOnArrayImg(segmenter, imgPlus);
		output = datasetService.create(outputImg);
	}

	private boolean useCache(ImgPlus<?> imgPlus) {
		return Intervals.numElements(imgPlus) > 100_000_000;
	}

	private Img<ShortType> calculateOnCachedImg(TrainableSegmentationSegmenter segmenter,
		ImgPlus<?> imgPlus)
	{
		Img<ShortType> outputImg = SegmentationUtils.createCachedSegmentation(segmenter, imgPlus, null);
		ParallelUtils.populateCachedImg(outputImg, new ProgressWriterConsole());
		return outputImg;
	}

	private Img<ShortType> calculateOnArrayImg(TrainableSegmentationSegmenter segmenter,
		ImgPlus<?> imgPlus)
	{
		Interval outputInterval = SegmentationUtils.intervalNoChannels(imgPlus);
		int[] cellSize = segmenter.suggestCellSize(imgPlus);
		Img<ShortType> outputImg = ArrayImgs.shorts(Intervals.dimensionsAsLongArray(outputInterval));
		ParallelUtils.applyOperationOnCells(outputImg, cellSize,
			outputCell -> segmenter.segment(imgPlus, outputCell), new ProgressWriterConsole());
		return outputImg;
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
