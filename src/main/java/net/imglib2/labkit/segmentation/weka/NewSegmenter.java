package net.imglib2.labkit.segmentation.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.imageserver.ImageRepository;
import net.imglib2.labkit.imageserver.dvid.ImageId;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class NewSegmenter implements Segmenter {

	private Notifier<Consumer<Segmenter>> listeners = new Notifier<>();

	public NewSegmenter(Context context, InputImage inputImage) {

	}

	// don't know why this exists
	public NewSegmenter(Context context, Object segmenter) {
	}

	@Override
	public void editSettings(JFrame dialogParent) {

	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> output) {

	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> output) {

	}

	@Override
	public void train(List<? extends RandomAccessibleInterval<?>> images, List<? extends Labeling> groundTruth) {
		// put image into the image repository
		// send a training request
		final ImageRepository imageRepository = ImageRepository.getInstance();
		for (RandomAccessibleInterval<?> image : images) {
			imageRepository.addImage(image);
		}
	}

	@Override
	public boolean isTrained() {
		return true;
	}

	@Override
	public void saveModel(String path, boolean overwrite) throws Exception {

	}

	@Override
	public void openModel(String path) throws Exception {

	}

	@Override
	public Notifier<Consumer<Segmenter>> listeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}
}
