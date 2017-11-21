package net.imglib2.atlas.classification.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.Labelings;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class TimeSeriesClassifier implements Classifier{

	private final Classifier classifier;
	private final Notifier<Listener> listeners = new Notifier<>();

	public TimeSeriesClassifier(Classifier classifier) {
		this.classifier = classifier;
		classifier.listeners().add(this::update);
	}

	private void update(Classifier classifier) {
		listeners.forEach(l -> l.notify(this));
	}

	@Override
	public void editClassifier() {
		classifier.editClassifier();
	}

	@Override
	public void reset(FeatureSettings features, List<String> classLabels) {
		classifier.reset(features, classLabels);
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels) {
		applyOnSlices(classifier::segment, image, labels);
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> prediction) {
		applyOnSlices(classifier::predict, image, prediction);
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
		classifier.train(images, labels);
	}

	@Override
	public boolean isTrained() {
		return classifier.isTrained();
	}

	@Override
	public void saveClassifier(String path, boolean overwrite) throws Exception {
		classifier.saveClassifier(path, overwrite);
	}

	@Override
	public void openClassifier(String path) throws Exception {
		classifier.openClassifier(path);
	}

	@Override
	public Notifier<Listener> listeners() {
		return listeners;
	}

	@Override
	public FeatureSettings settings() {
		return classifier.settings();
	}

	@Override
	public List<String> classNames() {
		return classifier.classNames();
	}
}
