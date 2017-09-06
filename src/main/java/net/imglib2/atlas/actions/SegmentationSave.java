package net.imglib2.atlas.actions;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
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
import org.scijava.ui.behaviour.util.RunnableAction;

import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class SegmentationSave extends AbstractSaveAndLoadAction {

	private final PredictionLayer predictionLayer;

	public SegmentationSave(MainFrame.Extensible extensible, PredictionLayer predictionLayer) {
		super(extensible, AbstractSaveAndLoadAction.TIFF_FILTER);
		this.predictionLayer = predictionLayer;
		initSaveAction("Save Segmentation", this::action, "");
		extensible.addAction(new RunnableAction("Show Segmentation", this::showSegmentation), "");
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
		Img<ShortType> img = predictionLayer.getPrediction();
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
		ParallelUtils.chunkOperation(img, cellDimensions, accessPixel);
	}

}
