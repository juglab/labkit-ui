package net.imglib2.labkit.actions;

import io.scif.img.ImgSaver;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.Extensible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
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

	public SegmentationSave(Extensible extensible, SegmentationResultsModel model ) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		this.extensible = extensible;
		Supplier<Img<ShortType>> segmentation = model::segmentation;
		initSaveAction("Save Segmentation ...", "saveSegmentation", getSaveAction(segmentation), "");
		extensible.addAction("Show Segmentation in ImageJ", "showSegmentation", getShowAction(segmentation), "");
		Supplier<Img<FloatType>> prediction = model::prediction;
		initSaveAction("Save Prediction ...", "savePrediction", getSaveAction(prediction), "");
		extensible.addAction("Show Prediction in ImageJ", "showPrediction", getShowAction(prediction), "");
	}

	private <T extends NumericType<T> & NativeType<T>> Runnable getShowAction(Supplier<Img<T>> supplier) {
		return () -> {
			ExecutorService executer = Executors.newSingleThreadExecutor();
			executer.submit( () -> {
				ImageJFunctions.show(LabkitUtils.populateCachedImg(supplier.get(), extensible.progressConsumer()));
			});
		};
	}

	private <T> Action getSaveAction(Supplier<Img<T>> supplier) {
		return filename -> {
			ImgSaver saver = new ImgSaver();
			saver.saveImg(filename, supplier.get());
		};
	}

}
