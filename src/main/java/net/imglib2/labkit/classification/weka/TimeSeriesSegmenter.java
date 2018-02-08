package net.imglib2.labkit.classification.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.classification.Segmenter;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.Labelings;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TimeSeriesSegmenter implements Segmenter
{

	private final Segmenter segmenter;
	private final Notifier< Consumer< Segmenter > > listeners = new Notifier<>();

	public TimeSeriesSegmenter(Segmenter segmenter ) {
		this.segmenter = segmenter;
		segmenter.listeners().add(this::update);
	}

	private void update(Segmenter segmenter ) {
		listeners.forEach(l -> l.accept(this));
	}

	@Override
	public void editSettings(Component dialogParent) {
		segmenter.editSettings(dialogParent);
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels) {
		applyOnSlices( segmenter::segment, image, labels);
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> prediction) {
		applyOnSlices( segmenter::predict, image, prediction);
	}

	private <T> void applyOnSlices(BiConsumer<RandomAccessibleInterval<?>, RandomAccessibleInterval<T>> action,
			RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<T> target) {
		int d = image.numDimensions() - 1;
		long min = target.min(d);
		long max = target.max(d);
		for (long pos = min; pos <= max; pos++)
			action.accept(Views.hyperSlice(image, d, pos), Views.hyperSlice(target, d, pos));
	}


	@Override
	public void train(List<? extends RandomAccessibleInterval<?>> image, List<? extends Labeling> groundTruth) {
		List<RandomAccessibleInterval<?>> images = image.stream().flatMap(i -> RevampUtils.slices(i).stream()).collect(Collectors.toList());
		List<Labeling> labels = groundTruth.stream().flatMap(g -> Labelings.slices(g).stream()).collect(Collectors.toList());
		segmenter.train(images, labels);
	}

	@Override
	public boolean isTrained() {
		return segmenter.isTrained();
	}

	@Override
	public void saveClassifier(String path, boolean overwrite) throws Exception {
		segmenter.saveClassifier(path, overwrite);
	}

	@Override
	public void openClassifier(String path) throws Exception {
		segmenter.openClassifier(path);
	}

	@Override
	public Notifier< Consumer< Segmenter > > listeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return segmenter.classNames();
	}
}
