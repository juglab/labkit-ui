
package net.imglib2.labkit.segmentation.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.Labelings;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TimeSeriesSegmenter implements Segmenter {

	private final Segmenter segmenter;
	private final Notifier<Runnable> listeners = new Notifier<>();

	public TimeSeriesSegmenter(Segmenter segmenter) {
		this.segmenter = segmenter;
		segmenter.trainingCompletedListeners().add(this::update);
	}

	private void update() {
		listeners.forEach(l -> l.run());
	}

	@Override
	public void editSettings(JFrame dialogParent) {
		segmenter.editSettings(dialogParent);
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> output)
	{
		applyOnSlices(segmenter::segment, image, output, 0);
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends RealType<?>> output)
	{
		applyOnSlices(segmenter::predict, image, output, 1);
	}

	private <T> void applyOnSlices(
		BiConsumer<RandomAccessibleInterval<?>, RandomAccessibleInterval<T>> action,
		RandomAccessibleInterval<?> image, RandomAccessibleInterval<T> target,
		int offset)
	{
		int imageTimeAxis = image.numDimensions() - 1;
		int targetTimeAxis = target.numDimensions() - 1 - offset;
		long min = target.min(targetTimeAxis);
		long max = target.max(targetTimeAxis);
		if (min < image.min(imageTimeAxis) || max > image.max(imageTimeAxis))
			throw new IllegalStateException("Last dimensions must fit.");
		for (long pos = min; pos <= max; pos++)
			action.accept(Views.hyperSlice(image, imageTimeAxis, pos), Views
				.hyperSlice(target, targetTimeAxis, pos));
	}

	@Override
	public void train(List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data) {
		throw new RuntimeException("Matthias you need to implement this method again!");
	}

	@Override
	public boolean isTrained() {
		return segmenter.isTrained();
	}

	@Override
	public void saveModel(String path) throws Exception {
		segmenter.saveModel(path);
	}

	@Override
	public void openModel(String path) throws Exception {
		segmenter.openModel(path);
	}

	@Override
	public Notifier<Runnable> trainingCompletedListeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return segmenter.classNames();
	}
}
