package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.composite.Composite;

import java.util.List;

public interface Classifier
{

	void reset(FeatureGroup features, List<String> classLabels);

	void predictLabels(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> instances, RandomAccessibleInterval<? extends IntegerType<?>> labels ) throws Exception;

	void trainClassifier(RandomAccessibleInterval<? extends Composite<? extends RealType<?>>> features, Labeling groundTruth);

	boolean isTrained();

	void saveClassifier( String path, boolean overwrite ) throws Exception;

	void loadClassifier( String path ) throws Exception;

	Notifier<Listener> listeners();

	FeatureGroup features();

	interface Listener
	{
		void notify( Classifier classifier, boolean applicable );
	}
}
