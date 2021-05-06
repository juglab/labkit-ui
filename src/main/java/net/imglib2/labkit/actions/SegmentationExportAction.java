
package net.imglib2.labkit.actions;

import net.imglib2.hdf5.HDF5Saver;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationResultsModel;
import bdv.export.ProgressWriter;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
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
			" in ImageJ", 201, item -> show(getResultsImage.apply(item.results(labelingModel))), null,
			"");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Calculate entire " + title, 300, item -> {
				final RandomAccessibleInterval<T> resultsImage =
					getResultsImage.apply(item.results(labelingModel));
				ParallelUtils.runInOtherThread(() -> {
					populate(resultsImage);
				});
			}, null, "");
	}

	private <T extends NumericType<T> & NativeType<T>> void populate(
		RandomAccessibleInterval<T> result)
	{
		final ProgressWriter progress = new SwingProgressWriter(null,
			"Segment Entire Image Volume");
		ParallelUtils.populateCachedImg(result, progress);
	}

	private <T extends NumericType<T> & NativeType<T>> void show(
		RandomAccessibleInterval<T> result)
	{
		populate(result);
		ParallelUtils.runInOtherThread(() -> ImageJFunctions.show(result));
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
