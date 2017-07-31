package net.imglib2.atlas.classification.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Training;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import java.util.Iterator;
import java.util.List;

public class TrainableSegmentationClassifier<I extends IntegerType< I > >
implements Classifier< Composite< FloatType >, RandomAccessibleInterval<FloatType>, RandomAccessibleInterval< I > >
{
	private net.imglib2.algorithm.features.Classifier classifier;

	private weka.classifiers.Classifier wekaClassifier;

	public TrainableSegmentationClassifier(final weka.classifiers.Classifier wekaClassifier, final List<String> classLabels, FeatureGroup features)
	{
		this.wekaClassifier = wekaClassifier;
		classifier = new net.imglib2.algorithm.features.Classifier(classLabels, features, wekaClassifier);
	}

	@Override
	synchronized public void predictLabels( final RandomAccessibleInterval<FloatType> instances, final RandomAccessibleInterval< I > labels ) throws Exception
	{
		this.<IntegerType>copy(classifier.applyOnFeatures(instances), labels);
	}

	private void copy(RandomAccessibleInterval<? extends IntegerType<?>> source, RandomAccessibleInterval<? extends IntegerType<?>> dest) {
		Views.interval(Views.pair(source, dest), dest).forEach(p -> p.getB().setInteger(p.getA().getInteger()));
	}

	@Override
	public void trainClassifier( final Iterable< Composite< FloatType > > samples, final int[] labels ) throws Exception
	{
		Training<net.imglib2.algorithm.features.Classifier> training = net.imglib2.algorithm.features.Classifier.training(classifier.classNames(), classifier.features(),
				wekaClassifier);

		int i = 0;
		Iterator<Composite<FloatType>> sample = samples.iterator();
		while(sample.hasNext())
			training.add(sample.next(), labels[i++]);

		classifier = training.train();
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
