package net.imglib2.labkit.classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import java.util.List;

// TODO: Rename to Segmenter and remote FeatureSettings from the interface
public interface Segmenter
{

	void editClassifier();

	void reset(FeatureSettings features, List<String> classLabels);

	void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels );

	void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> prediction);

	void train(List<? extends RandomAccessibleInterval<?>> image, List<? extends Labeling> groundTruth);

	boolean isTrained();

	void saveClassifier( String path, boolean overwrite ) throws Exception;

	void openClassifier( String path ) throws Exception;

	Notifier<Listener> listeners();

	FeatureSettings settings();

	List<String> classNames();

	interface Listener
	{
		void notify(Segmenter segmenter );
	}
}
