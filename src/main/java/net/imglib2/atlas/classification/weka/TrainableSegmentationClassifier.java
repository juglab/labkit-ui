package net.imglib2.atlas.classification.weka;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ops.OpEnvironment;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.FeatureStack;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.actions.SelectClassifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.roi.IterableRegion;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.classification.Training;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import weka.classifiers.AbstractClassifier;

import java.util.List;
import java.util.Map;


public class TrainableSegmentationClassifier
implements Classifier
{
	private final OpEnvironment ops;

	private weka.classifiers.Classifier initialWekaClassifier;

	private Segmenter classifier;

	private final Notifier< Listener > listeners = new Notifier<>();

	private boolean isTrained = false;

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

	@Override
	public void editClassifier() {
		initialWekaClassifier = SelectClassifier.runStatic(null, initialWekaClassifier);
		reset(classifier.settings(), classifier.classNames());
	}

	@Override
	public void reset(FeatureSettings settings, List<String> classLabels) {
		weka.classifiers.Classifier wekaClassifier = RevampUtils.wrapException(() ->
				AbstractClassifier.makeCopy(this.initialWekaClassifier));
		reset(new Segmenter(ops, classLabels, settings, wekaClassifier));
	}

	private void reset(Segmenter classifier) {
		this.classifier = classifier;
		isTrained = false;
		listeners.forEach(l -> l.notify(this));
	}

	public TrainableSegmentationClassifier(OpEnvironment ops, weka.classifiers.Classifier initialWekaClassifier, final List<String> classLabels, FeatureSettings features)
	{
		this.ops = ops;
		this.initialWekaClassifier = initialWekaClassifier;
		reset(features, classLabels);
	}

	public TrainableSegmentationClassifier(OpEnvironment ops, Segmenter classifier)
	{
		this.ops = ops;
		this.initialWekaClassifier = new FastRandomForest();
		this.classifier = classifier;
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels) {
		classifier.segment(labels, Views.extendBorder(image));
	}

	@Override
	public void train(RandomAccessibleInterval<?> image, Labeling labeling) {
		Training training = classifier.training();
		Map<String, IterableRegion<BitType>> regions = labeling.iterableRegions();
		List<String> classes = classifier.classNames();
		RandomAccessible<? extends Composite<FloatType>> features = Views.collapse(FeatureStack.cachedFeatureBlock(classifier.features(), image));
		for (int classIndex = 0; classIndex < classes.size(); classIndex++) {
			IterableRegion<BitType> region = regions.get(classes.get(classIndex));
			Cursor<Void> cursor = region.cursor();
			RandomAccess<? extends Composite<? extends RealType<?>>> ra = features.randomAccess();
			while (cursor.hasNext()) {
				cursor.fwd();
				ra.setPosition(cursor);
				training.add(ra.get(), classIndex);
			}
		}
		training.train();
		isTrained = true;
		listeners.forEach(l -> l.notify(this));
	}

	@Override
	public boolean isTrained() {
		return isTrained;
	}

	@Override
	synchronized public void saveClassifier( final String path, final boolean overwrite ) throws Exception
	{
		GsonUtils.write(classifier.toJsonTree(), path);
	}

	@Override
	public void openClassifier( final String path ) throws Exception
	{
		classifier = Segmenter.fromJson(ops, GsonUtils.read(path));
		isTrained = true;
		listeners.forEach(l -> l.notify(this));
	}
}
