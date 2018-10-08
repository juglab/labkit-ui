
package net.imglib2.labkit.actions;

import io.scif.img.ImgSaver;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgView;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractFileIoAction {

	private final Extensible extensible;

	public SegmentationSave(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter)
	{
		super(extensible, AbstractFileIoAction.TIFF_FILTER);
		this.extensible = extensible;
		addMenuItems(selectedSegmenter, item -> item.results().segmentation(),
			"SegmentationResult", "Segmentation");
		addMenuItems(selectedSegmenter, item -> item.results().prediction(),
			"Probability Map", "Prediction");
	}

	private <T extends NumericType<T> & NativeType<T>> void addMenuItems(
		Holder<SegmentationItem> selectedSegmenter,
		Function<SegmentationItem, RandomAccessibleInterval<T>> predictionFactory,
		String title, String commandKey)
	{
		Supplier<RandomAccessibleInterval<T>> selectedResult =
			() -> predictionFactory.apply(selectedSegmenter.get());
		initSaveAction("Save " + title + " ...", "save" + commandKey, getSaveAction(
			selectedResult), "");
		extensible.addAction("Show " + title + " in ImageJ", "show" + commandKey,
			getShowAction(selectedResult), "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Save " + title +
			" ...", item -> openDialogAndThen("Save " + title + " ...",
				JFileChooser.OPEN_DIALOG, getSaveAction(() -> predictionFactory.apply(
					item))), null);
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU, "Show " + title +
			" in ImageJ", item -> getShowAction(() -> predictionFactory.apply(item))
				.run(), null);
	}

	private <T extends NumericType<T> & NativeType<T>> Runnable getShowAction(
		Supplier<RandomAccessibleInterval<T>> supplier)
	{
		return () -> {
			ExecutorService executer = Executors.newSingleThreadExecutor();
			executer.submit(() -> {
				ImageJFunctions.show(LabkitUtils.populateCachedImg(supplier.get(),
					extensible.progressConsumer()));
			});
		};
	}

	private <T extends Type<T>> Action getSaveAction(
		Supplier<RandomAccessibleInterval<T>> supplier)
	{
		return filename -> {
			ImgSaver saver = new ImgSaver();
			saver.saveImg(filename, ImgView.wrap(supplier.get(), null));
		};
	}

}
