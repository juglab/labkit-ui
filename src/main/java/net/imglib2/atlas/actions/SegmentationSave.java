package net.imglib2.atlas.actions;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.Extensible;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.ParallelUtils;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractFileIoAcion {

	private final PredictionLayer predictionLayer;

	public SegmentationSave(Extensible extensible, PredictionLayer predictionLayer) {
		super(extensible, AbstractFileIoAcion.TIFF_FILTER);
		this.predictionLayer = predictionLayer;
		initSaveAction("Save Segmentation ...", "saveSegmentation", this::action, "");
		extensible.addAction("Show Segmentation", "showSegmentation", this::showSegmentation, "");
	}

	private void action(String filename) throws IncompatibleTypeException, ImgIOException {
		Img<ShortType> img = getSegmentation();
		ImgSaver saver = new ImgSaver();
		saver.saveImg(filename, img);
	}

	private void showSegmentation() {
		Img<ShortType> image = getSegmentation();
		ImageJFunctions.show(image);
	}

	private Img<ShortType> getSegmentation() {
		Img<ShortType> img = predictionLayer.prediction();
		populateCachedImg(img);
		return img;
	}

	private void populateCachedImg(Img<ShortType> img) {
		if(img instanceof CachedCellImg)
			internPopulateCachedImg((CachedCellImg<ShortType, ?>) img);
	}

	private <T extends NumericType<T> & NativeType<T>> void internPopulateCachedImg(CachedCellImg<T, ?> img) {
		int[] cellDimensions = new int[img.getCellGrid().numDimensions()];
		img.getCellGrid().cellDimensions(cellDimensions);
		T t = img.randomAccess().get();
		Consumer<RandomAccessibleInterval<T>> accessPixel = target -> {
			long[] min = Intervals.minAsLongArray(target);
			RandomAccess<T> ra = target.randomAccess();
			ra.setPosition(min);
			ra.get().valueEquals(t);
		};
		List<Callable<Void>> tasks = ParallelUtils.chunkOperation(img, cellDimensions, accessPixel);
		ParallelUtils.executeInParallel(
				Executors.newFixedThreadPool(10),
				ParallelUtils.addShowProgress(tasks)
		);
	}

}
