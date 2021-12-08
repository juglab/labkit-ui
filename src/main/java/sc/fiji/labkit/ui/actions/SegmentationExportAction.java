
package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.utils.HDF5Saver;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationResultsModel;
import bdv.export.ProgressWriter;
import sc.fiji.labkit.ui.utils.ParallelUtils;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.util.function.Function;

/**
 * Implements the menu items that allow to save the segmentation and probability
 * map.
 *
 * @author Matthias Arzt
 */
public class SegmentationExportAction extends AbstractFileIoAction {

	private final ImageLabelingModel labelingModel;

	public SegmentationExportAction(Extensible extensible,
		ImageLabelingModel labelingModel)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER,
			AbstractFileIoAction.HDF5_FILTER);
		this.labelingModel = labelingModel;
		addMenuItems(SegmentationResultsModel::segmentation, "Segmentation Result");
		addMenuItems(SegmentationResultsModel::prediction, "Probability Map");
	}

	private <T extends NumericType<T> & NativeType<T>> void addMenuItems(
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		String title)
	{
		initSaveAction(SegmentationItem.SEGMENTER_MENU, "Save " + title +
			" as TIF / HDF5 ...", 200, (item, filename) -> saveImage(filename,
				getResultsImage.apply(item.results(labelingModel))), "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Show " + title +
			" in ImageJ", 201, item -> onShowResultInImageJClicked(getResultsImage, item), null,
			"");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Calculate entire " + title, 300, item -> {
				onCalculateEntireResultClicked(getResultsImage, item);
			}, null, "");
	}

	private <T extends NumericType<T> & NativeType<T>> void onShowResultInImageJClicked(
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		SegmentationItem item)
	{
		RandomAccessibleInterval<T> result = getResultsImage.apply(item.results(labelingModel));
		ParallelUtils.runInOtherThread(() -> populate(result));
		ParallelUtils.runInOtherThread(() -> ImageJFunctions.show(result));
	}

	private <T extends NumericType<T> & NativeType<T>> void onCalculateEntireResultClicked(
		Function<SegmentationResultsModel, RandomAccessibleInterval<T>> getResultsImage,
		SegmentationItem item)
	{
		final RandomAccessibleInterval<T> resultsImage = getResultsImage.apply(item.results(
			labelingModel));
		ParallelUtils.runInOtherThread(() -> populate(resultsImage));
	}

	private <T extends NumericType<T> & NativeType<T>> void populate(
		RandomAccessibleInterval<T> result)
	{
		final ProgressWriter progress = new SwingProgressWriter(null,
			"Segment Entire Image Volume");
		ParallelUtils.populateCachedImg(result, progress);
	}

	private <T extends Type<T>> void saveImage(String filename,
		RandomAccessibleInterval<T> image)
	{
		if (filename.endsWith(".h5") || filename.endsWith(".xml")) {
			final HDF5Saver saver = new HDF5Saver(image, filename);
			saver.setProgressWriter(new SwingProgressWriter(extensible.dialogParent(),
				"Save Image"));
			saver.writeAll();
		}
		else {
			try {
				ImgSaver saver = new ImgSaver(extensible.context());
				saver.saveImg(filename, ImgView.wrap(image, null));
			}
			catch (io.scif.img.ImgIOException e) {
				if (e.getCause() instanceof io.scif.FormatException) JOptionPane
					.showMessageDialog(null, "File format not supported:\n" + filename);
				else throw e;
			}
		}
	}
}
