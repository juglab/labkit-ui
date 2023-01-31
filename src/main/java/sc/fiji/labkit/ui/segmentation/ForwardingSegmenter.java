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

import javax.swing.*;
import java.util.List;

/**
 * Simple implementation of a {@link Segmenter}, that forwards every method call
 * to a "source" Segmenter.
 */
public class ForwardingSegmenter implements Segmenter {

	private final Segmenter source;

	public ForwardingSegmenter(Segmenter source) {
		this.source = source;
	}

	protected Segmenter getSourceSegmenter() {
		return source;
	}

	@Override
	public void editSettings(JFrame dialogParent, List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		source.editSettings(dialogParent, trainingData);
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		source.train(trainingData);
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		source.segment(image, outputSegmentation);
	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		source.predict(image, outputProbabilityMap);
	}

	@Override
	public void setUseGpu(boolean useGpu) {
		source.setUseGpu(useGpu);
	}

	@Override
	public boolean isTrained() {
		return source.isTrained();
	}

	@Override
	public void saveModel(String path) {
		source.saveModel(path);
	}

	@Override
	public void openModel(String path) {
		source.openModel(path);
	}

	@Override
	public List<String> classNames() {
		return source.classNames();
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		return source.suggestCellSize(image);
	}

	@Override
	public boolean requiresFixedCellSize() {
		return source.requiresFixedCellSize();
	}
}
