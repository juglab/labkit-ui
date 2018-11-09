package net.imglib2.labkit_rest_api;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit_rest_api.ImageRepository;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RestSegmenter implements Segmenter {

	private Notifier<Consumer<Segmenter>> listeners = new Notifier<>();

	public RestSegmenter(Context context, InputImage inputImage) {

	}

	public RestSegmenter(Context context, Object segmenter) {
		throw new RuntimeException("TODO, remove the usage of this constructor. It makes no sense.");
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
			imageRepository.addImage("image", image);
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
