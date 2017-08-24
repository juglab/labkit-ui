package net.imglib2.atlas.classification.weka;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.classification.Training;
import net.imglib2.atlas.FeatureStack;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TrainableSegmentationClassifier
implements Classifier
{
	private final Supplier<weka.classifiers.Classifier> wekaClassifierFactory;

	private net.imglib2.algorithm.features.classification.Classifier classifier;

	private final Notifier< Listener > listeners = new Notifier<>();

	private boolean isTrained = false;

	@Override
	public Notifier<Listener> listeners() {
		return listeners;
	}

	@Override
	public FeatureGroup features() {
		return classifier.features();
	}

	@Override
	public List<String> classNames() {
		return classifier.classNames();
	}

	@Override
	public void reset(FeatureGroup features, List<String> classLabels) {
		classifier = new net.imglib2.algorithm.features.classification.Classifier(classLabels, features, wekaClassifierFactory.get());
		isTrained = false;
		listeners.forEach(l -> l.notify(this));
	}

	public TrainableSegmentationClassifier(Supplier<weka.classifiers.Classifier> wekaClassifierFactory, final List<String> classLabels, FeatureGroup features)
	{
		this.wekaClassifierFactory = wekaClassifierFactory;
		reset(features, classLabels);
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels) throws Exception {
		classifier.segment(labels, Views.extendBorder(image));
	}

	@Override
	public void train(RandomAccessibleInterval<?> image, Labeling labeling) {
		Training training = classifier.training();
		Map<String, IterableRegion<BitType>> regions = labeling.regions();
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
		classifier.store(path);
	}

	@Override
	public void loadClassifier( final String path ) throws Exception
	{
		classifier = net.imglib2.algorithm.features.classification.Classifier.load(path);
		isTrained = true;
		listeners.forEach(l -> l.notify(this));
	}
}
