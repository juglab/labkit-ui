
package net.imglib2.labkit.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import javax.swing.JFrame;
import java.util.List;

public interface Segmenter {

	void editSettings(JFrame dialogParent);

	/**
	 * If the requirements for training are not met, and you want to give an
	 * advice to the user, simple throw a
	 * {@link java.util.concurrent.CancellationException} that describes what has
	 * to be fixed.
	 */
	void train(List<Pair<? extends RandomAccessibleInterval<?>,
			? extends Labeling>> image);

	void segment(RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<? extends IntegerType<?>> output);

	void predict(RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<? extends RealType<?>> output);

	boolean isTrained();

	void saveModel(String path) throws Exception;

	void openModel(String path) throws Exception;

	Notifier<Runnable> trainingCompletedListeners();

	List<String> classNames();
}
