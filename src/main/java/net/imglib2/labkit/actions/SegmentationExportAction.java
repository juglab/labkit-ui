
package net.imglib2.labkit.actions;

import bdv.export.HDF5Saver;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Matthias Arzt
 */
public class SegmentationExportAction extends AbstractFileIoAction {

	private final Extensible extensible;

	public SegmentationExportAction(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER,
			AbstractFileIoAction.HDF5_FILTER);
		this.extensible = extensible;
		addMenuItems(selectedSegmenter, item -> item.results().segmentation(),
			"Segmentation Result");
		addMenuItems(selectedSegmenter, item -> item.results().prediction(),
			"Probability Map");
	}

	private <T extends NumericType<T> & NativeType<T>> void addMenuItems(
		Holder<SegmentationItem> selectedSegmenter,
		Function<SegmentationItem, RandomAccessibleInterval<T>> predictionFactory,
		String title)
	{
		Supplier<RandomAccessibleInterval<T>> selectedResult =
			() -> predictionFactory.apply(selectedSegmenter.get());
		initSaveAction(MenuBar.SEGMENTER_MENU, "Save " + title + " ...", 200,
			getSaveAction(selectedResult), "");
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU, "Show " + title +
			" in ImageJ", 201, ignore -> getShowAction(selectedResult).run(), null,
			"");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Save " + title +
			" as TIF / HDF5 ...", 202, item -> openDialogAndThen("Save " + title +
				" ...", JFileChooser.SAVE_DIALOG, getSaveAction(() -> predictionFactory
					.apply(item))), null, null);
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Show " + title +
			" in ImageJ", 203, item -> getShowAction(() -> predictionFactory.apply(
				item)).run(), null, null);
	}

	private <T extends NumericType<T> & NativeType<T>> Runnable getShowAction(
		Supplier<RandomAccessibleInterval<T>> supplier)
	{
		return startInNewThread(() -> {
			final RandomAccessibleInterval<T> img = LabkitUtils.populateCachedImg(
				supplier.get(), extensible.progressConsumer());
			ImageJFunctions.show(img);
		});
	}

	private Runnable startInNewThread(Runnable action) {
		return () -> {
			ExecutorService executer = Executors.newSingleThreadExecutor();
			executer.submit(() -> {
				action.run();
				executer.shutdown();
			});
		};
	}

	private <T extends Type<T>> Action getSaveAction(
		Supplier<RandomAccessibleInterval<T>> supplier)
	{
		return filename -> saveImage(filename, supplier.get());
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
