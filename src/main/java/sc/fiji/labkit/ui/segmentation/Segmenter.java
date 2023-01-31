/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.segmentation;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.ui.labeling.Labeling;
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
	void editSettings(JFrame dialogParent, List<Pair<ImgPlus<?>, Labeling>> trainingData);

	/**
	 * Train the model with the given data.
	 * <p>
	 * If the requirements for training are not met, and you want to give an advice
	 * to the user, simple throw a
	 * {@link java.util.concurrent.CancellationException} that describes what has to
	 * be fixed.
	 * <p>
	 * Blocks until training is done.
	 */
	void train(List<Pair<ImgPlus<?>, Labeling>> trainingData);

	/**
	 * Segment the image and write the result into the provided output. The output
	 * might be smaller than the image, in this case only the chunk specified by the
	 * output's interval is segmented.
	 * <p>
	 * Blocks until segmentation is done.
	 *
	 * @param image Image to be segmented.
	 * @param outputSegmentation Buffer to hold the result. Pixel value is the index
	 *          of the class, as returned by {@link #classNames()}
	 */
	void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation);

	/**
	 * Segment the image and write the probability distribution (probability map)
	 * into the provided output. The output has therefor one more axis than the
	 * input. The output might be smaller than the image, in this case only the
	 * chunk specified by the output's interval is segmented.
	 * <p>
	 * Blocks until probabilities are calculated.
	 *
	 * @param image Image to be segmented.
	 * @param outputProbabilityMap Buffer to hold the result. Pixel value is the
	 *          index of the class, as returned by {@link #classNames()}
	 */
	void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap);

	default void setUseGpu(boolean useGpu) {}

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

	int[] suggestCellSize(ImgPlus<?> image);

	boolean requiresFixedCellSize();
}
