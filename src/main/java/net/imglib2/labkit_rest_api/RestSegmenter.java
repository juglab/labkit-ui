package net.imglib2.labkit_rest_api;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RestSegmenter implements Segmenter {

	private Notifier<Runnable> listeners = new Notifier<>();

	public RestSegmenter(Context context, InputImage inputImage) {

	}

	public RestSegmenter(Context context, Object segmenter) {
		throw new RuntimeException("TODO, remove the usage of this constructor. It makes no sense.");
	}

	@Override
	public void editSettings(JFrame dialogParent) {

	}

	@Override
	public void train(List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data) {
		// put image into the image repository
		final ImageRepository imageRepository = ImageRepository.getInstance();
		for (Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> imageAndLabels : data) {
			imageRepository.addImage("image", imageAndLabels.getA());
		}
		// send a training request
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> output) {

	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> output) {

	}

	@Override
	public boolean isTrained() {
		return true;
	}

	@Override
	public void saveModel(String path) throws Exception {

	}

	@Override
	public void openModel(String path) throws Exception {

	}

	@Override
	public Notifier<Runnable> trainingCompletedListeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}
}
