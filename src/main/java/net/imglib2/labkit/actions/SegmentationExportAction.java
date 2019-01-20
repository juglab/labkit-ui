
package net.imglib2.labkit.actions;

import bdv.export.HDF5Saver;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.utils.LabkitUtils;
import bdv.export.ProgressWriter;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.util.function.Function;

/**
 * @author Matthias Arzt
 */
public class SegmentationExportAction extends AbstractFileIoAction {

	private final Extensible extensible;

	public SegmentationExportAction(Extensible extensible) {
		super(extensible, AbstractFileIoAction.TIFF_FILTER,
			AbstractFileIoAction.HDF5_FILTER);
		this.extensible = extensible;
		addMenuItems(item -> item.results().segmentation(), "Segmentation Result");
		addMenuItems(item -> item.results().prediction(), "Probability Map");
	}

	private <T extends NumericType<T> & NativeType<T>> void addMenuItems(
		Function<SegmentationItem, RandomAccessibleInterval<T>> predictionFactory,
		String title)
	{
		initSaveAction(SegmentationItem.SEGMENTER_MENU, "Save " + title +
			" as TIF / HDF5 ...", 200, (data, filename) -> saveImage(filename,
				predictionFactory.apply(data)), "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Show " + title +
			" in ImageJ", 201, data -> show(predictionFactory.apply(data)), null, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Calculate entire " + title, 300, item -> populate(predictionFactory
				.apply(item)), null, "");
	}

	private <T extends NumericType<T> & NativeType<T>> void populate(
		RandomAccessibleInterval<T> result)
	{
		ParallelUtils.runInOtherThread(() -> populate2(result));
	}

	private <T extends NumericType<T> & NativeType<T>> void populate2(
		RandomAccessibleInterval<T> result)
	{
		final ProgressWriter progress = new SwingProgressWriter(null,
			"Segment Entire Image Volume");
		LabkitUtils.populateCachedImg(result, progress);
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
			final HDF5Saver saver = new HDF5Saver();
			saver.setProgressWriter(new SwingProgressWriter(extensible.dialogParent(),
				"Save Image"));
			saver.save(filename, image);
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
