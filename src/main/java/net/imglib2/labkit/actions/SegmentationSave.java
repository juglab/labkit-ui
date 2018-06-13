
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
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractFileIoAcion {

	private final Extensible extensible;

	public SegmentationSave(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter)
	{
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		this.extensible = extensible;
		Supplier<RandomAccessibleInterval<ShortType>> segmentationSupplier =
			() -> selectedSegmenter.get().results().segmentation();
		initSaveAction("Save Segmentation ...", "saveSegmentation", getSaveAction(
			segmentationSupplier), "");
		extensible.addAction("Show Segmentation in ImageJ", "showSegmentation",
			getShowAction(segmentationSupplier), "");
		Supplier<RandomAccessibleInterval<FloatType>> predictionSupplier =
			() -> selectedSegmenter.get().results().prediction();
		initSaveAction("Save Prediction ...", "savePrediction", getSaveAction(
			predictionSupplier), "");
		extensible.addAction("Show Prediction in ImageJ", "showPrediction",
			getShowAction(predictionSupplier), "");
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
