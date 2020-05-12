
package net.imglib2.labkit.segmentation.weka;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.Labelings;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TimeSeriesSegmenter implements Segmenter {

	private final Segmenter segmenter;

	public TimeSeriesSegmenter(Segmenter segmenter) {
		this.segmenter = segmenter;
	}

	@Override
	public void editSettings(JFrame dialogParent) {
		segmenter.editSettings(dialogParent);
	}

	@Override
	public void segment(ImgPlus<?> image, RandomAccessibleInterval<? extends IntegerType<?>> output) {
		applyOnSlices(segmenter::segment, image, output, 0);
	}

	@Override
	public void predict(ImgPlus<?> image, RandomAccessibleInterval<? extends RealType<?>> output) {
		applyOnSlices(segmenter::predict, image, output, 1);
	}

	private <T> void applyOnSlices(
		BiConsumer<ImgPlus<?>, RandomAccessibleInterval<T>> action,
		ImgPlus<?> image, RandomAccessibleInterval<T> target,
		int offset)
	{
		int imageTimeAxis = image.dimensionIndex(Axes.TIME);
		int targetTimeAxis = target.numDimensions() - 1 - offset;
		long min = target.min(targetTimeAxis);
		long max = target.max(targetTimeAxis);
		if (min < image.min(imageTimeAxis) || max > image.max(imageTimeAxis))
			throw new IllegalStateException("Last dimensions must fit.");
		for (long pos = min; pos <= max; pos++)
			action.accept(ImgPlusViews.hyperSlice(Cast.unchecked(image), imageTimeAxis, pos), Views
				.hyperSlice(target, targetTimeAxis, pos));
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		List<Pair<ImgPlus<?>, Labeling>> slicedData =
			trainingData.stream().flatMap(this::slice).collect(Collectors.toList());
		segmenter.train(slicedData);
	}

	private Stream<Pair<ImgPlus<?>, Labeling>> slice(Pair<ImgPlus<?>, Labeling> pair) {
		List<ImgPlus<?>> imageSlices = ImgPlusViewsOld.hyperSlices(pair.getA(), Axes.TIME);
		// FIXME: does this work?
		List<Labeling> labelSlices = Labelings.slices(pair.getB());
		return IntStream.range(0, imageSlices.size()).mapToObj(i -> new ValuePair<>(
			imageSlices.get(i), labelSlices.get(i)));
	}

	@Override
	public boolean isTrained() {
		return segmenter.isTrained();
	}

	@Override
	public void saveModel(String path) {
		segmenter.saveModel(path);
	}

	@Override
	public void openModel(String path) {
		segmenter.openModel(path);
	}

	@Override
	public List<String> classNames() {
		return segmenter.classNames();
	}
}
