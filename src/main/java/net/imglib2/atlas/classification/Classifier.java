package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;

import java.util.List;

public interface Classifier
{

	void editClassifier();

	void reset(FeatureGroup features, List<String> classLabels);

	void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels );

	void train(RandomAccessibleInterval<?> image, Labeling groundTruth);

	boolean isTrained();

	void saveClassifier( String path, boolean overwrite ) throws Exception;

	void loadClassifier( String path ) throws Exception;

	Notifier<Listener> listeners();

	FeatureGroup features();

	List<String> classNames();

	interface Listener
	{
		void notify(Classifier classifier);
	}
}
