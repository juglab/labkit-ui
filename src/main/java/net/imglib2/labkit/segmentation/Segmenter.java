
package net.imglib2.labkit.segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import javax.swing.JFrame;
import java.util.List;

/**
 * Interface for a segmentation model.
 */
public interface Segmenter {

	/**
	 * Show a GUI dialog and allow the user to edit the settings of the model.
	 */
	void editSettings(JFrame dialogParent);

	/**
	 * Train the model with the given data.
	 * <p>
	 * If the requirements for training are not met, and you want to give an
	 * advice to the user, simple throw a
	 * {@link java.util.concurrent.CancellationException} that describes what has
	 * to be fixed.
	 */
	void train(
		List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> image);

	/**
	 * Segment the image and write the result into the provided output. The output
	 * might be smaller than the image, in this case only the chunk specified by
	 * the output's interval is segmented.
	 * 
	 * @param image Image to be segmented.
	 * @param outputSegmentation Buffer to hold the result. Pixel value is the
	 *          index of the class, as returned by {@link #classNames()}
	 */
	void segment(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation);

	/**
	 * Segment the image and write the probability distribution (probability map)
	 * into the provided output. The output has therefor one more axis than the
	 * input. The output might be smaller than the image, in this case only the
	 * chunk specified by the output's interval is segmented.
	 * 
	 * @param image Image to be segmented.
	 * @param outputProbabilityMap Buffer to hold the result. Pixel value is the
	 *          index of the class, as returned by {@link #classNames()}
	 */
	void predict(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap);

	/**
	 * Return true if the model is trained.
	 */
	boolean isTrained();

	/**
	 * Save the model to the given file.
	 */
	void saveModel(String path);

	/**
	 * Load the model from the given file.
	 */
	void openModel(String path);

	/**
	 * Return a list of classes, this segmenter return. For example ["background",
	 * "foreground"]
	 */
	List<String> classNames();
}
