package net.imglib2.atlas.classification.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Training;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import java.util.Iterator;
import java.util.List;

public class TrainableSegmentationClassifier
implements Classifier
{
	private net.imglib2.algorithm.features.Classifier classifier;

	private weka.classifiers.Classifier wekaClassifier;

	public TrainableSegmentationClassifier(final weka.classifiers.Classifier wekaClassifier, final List<String> classLabels, FeatureGroup features)
	{
		this.wekaClassifier = wekaClassifier;
		classifier = new net.imglib2.algorithm.features.Classifier(classLabels, features, wekaClassifier);
	}

	@Override
	public void predictLabels(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> instances, RandomAccessibleInterval<? extends IntegerType<?>> labels) throws Exception {
		this.<IntegerType>copy(classifier.applyOnComposite(instances), labels);
	}

	private void copy(RandomAccessibleInterval<? extends IntegerType<?>> source, RandomAccessibleInterval<? extends IntegerType<?>> dest) {
		Views.interval(Views.pair(source, dest), dest).forEach(p -> p.getB().setInteger(p.getA().getInteger()));
	}

	@Override
	public void trainClassifier(Iterator<Pair<Composite<? extends RealType<?>>, ? extends IntegerType<?>>> data) throws Exception {
		Training<net.imglib2.algorithm.features.Classifier> training = net.imglib2.algorithm.features.Classifier.training(classifier.classNames(), classifier.features(),
				wekaClassifier);

		while(data.hasNext()) {
			Pair<Composite<? extends RealType<?>>, ? extends IntegerType<?>> pair = data.next();
			training.add(pair.getA(), pair.getB().getInteger());
		}

		classifier = training.train();
	}

	@Override
	public boolean isTrained() {
		return false;
	}

	@Override
	synchronized public void saveClassifier( final String path, final boolean overwrite ) throws Exception
	{
		classifier.store(path);
	}

	@Override
	public void loadClassifier( final String path ) throws Exception
	{
		classifier = net.imglib2.algorithm.features.Classifier.load(path);
	}
}
