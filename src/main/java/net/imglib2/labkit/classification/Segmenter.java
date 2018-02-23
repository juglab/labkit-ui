package net.imglib2.labkit.classification;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

// TODO: Rename to Segmenter and remote FeatureSettings from the interface
public interface Segmenter
{

	void editSettings(JFrame dialogParent);

	void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels );

	void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> prediction);

	void train(List<? extends RandomAccessibleInterval<?>> image, List<? extends Labeling> groundTruth);

	boolean isTrained();

	void saveClassifier( String path, boolean overwrite ) throws Exception;

	void openClassifier( String path ) throws Exception;

	Notifier<Consumer<Segmenter> > listeners();

	List<String> classNames();
}
