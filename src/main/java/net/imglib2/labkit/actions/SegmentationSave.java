package net.imglib2.labkit.actions;

import io.scif.img.ImgSaver;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.classification.PredictionLayer;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.function.Supplier;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractFileIoAcion {

	public SegmentationSave(Extensible extensible, PredictionLayer predictionLayer) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		Supplier<Img<ShortType>> segmentation = predictionLayer::segmentation;
		initSaveAction("Save Segmentation ...", "saveSegmentation", getSaveAction(segmentation), "");
		extensible.addAction("Show Segmentation in ImageJ", "showSegmentation", getShowAction(segmentation), "");
		Supplier<Img<FloatType>> prediction = predictionLayer::prediction;
		initSaveAction("Save Prediction ...", "savePrediction", getSaveAction(prediction), "");
		extensible.addAction("Show Prediction in ImageJ", "showPrediction", getShowAction(prediction), "");
	}

	private <T extends NumericType<T> & NativeType<T>> Runnable getShowAction(Supplier<Img<T>> supplier) {
		return () -> ImageJFunctions.show(LabkitUtils.populateCachedImg(supplier.get()));
	}

	private <T> Action getSaveAction(Supplier<Img<T>> supplier) {
		return filename -> {
			ImgSaver saver = new ImgSaver();
			saver.saveImg(filename, supplier.get());
		};
	}

}
