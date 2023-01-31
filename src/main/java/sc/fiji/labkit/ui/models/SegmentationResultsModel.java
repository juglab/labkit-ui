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

package sc.fiji.labkit.ui.models;

import net.imagej.ImgPlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.segmentation.SegmentationUtils;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.ui.utils.Notifier;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * SegmentationResultsModel is segmentation + probability map. It and updates
 * whenever the Segmentation changes. It's possible to listen to the
 * SegmentationResultsModel.
 */
public class SegmentationResultsModel {

	private final ExtensionPoints extensionPoints;
	private final ImageLabelingModel model;
	private final Segmenter segmenter;
	private boolean hasResults = false;
	private RandomAccessibleInterval<UnsignedByteType> segmentation;
	private RandomAccessibleInterval<FloatType> prediction;
	private List<String> labels = Collections.emptyList();
	private List<ARGBType> colors = Collections.emptyList();

	private final Notifier listeners = new Notifier();

	public SegmentationResultsModel(ImageLabelingModel model, ExtensionPoints extensionPoints,
		Segmenter segmenter)
	{
		this.model = model;
		this.extensionPoints = extensionPoints;
		this.segmenter = segmenter;
		segmentation = dummy(new UnsignedByteType());
		prediction = dummy(new FloatType());
		model.imageForSegmentation().notifier().addListener(this::update);
		update();
	}

	public ImageLabelingModel imageLabelingModel() {
		return model;
	}

	public void update() {
		if (segmenter.isTrained()) {
			updateSegmentation(segmenter);
			updatePrediction(segmenter);
			this.labels = segmenter.classNames();
			this.colors = this.labels.stream().map(this::getLabelColor).collect(
				Collectors.toList());
			hasResults = true;
			listeners.notifyListeners();
		}
	}

	private ARGBType getLabelColor(String name) {
		Labeling labeling = model.labeling().get();
		try {
			return labeling.getLabel(name).color();
		}
		catch (NoSuchElementException e) {
			// NB: Catching this exception is an ugly bugfix, a cleaner solution
			// would be better.
			Label label = labeling.addLabel(name);
			model.labeling().notifier().notifyListeners();
			return label.color();
		}
	}

	public void clear() {
		segmentation = dummy(new UnsignedByteType());
		prediction = dummy(new FloatType());
		hasResults = false;
		listeners.notifyListeners();
	}

	public RandomAccessibleInterval<UnsignedByteType> segmentation() {
		return segmentation;
	}

	private <T> RandomAccessibleInterval<T> dummy(T value) {
		FinalInterval interval = new FinalInterval(model.imageForSegmentation().get());
		return ConstantUtils.constantRandomAccessibleInterval(value, interval);
	}

	public RandomAccessibleInterval<FloatType> prediction() {
		return prediction;
	}

	private void updatePrediction(Segmenter segmenter) {
		ImgPlus<?> image = model.imageForSegmentation().get();
		this.prediction = SegmentationUtils.createCachedProbabilityMap(segmenter, image,
			extensionPoints.getCachedPredictionImageFactory());
	}

	private void updateSegmentation(Segmenter segmenter) {
		ImgPlus<?> image = model.imageForSegmentation().get();
		this.segmentation = SegmentationUtils.createCachedSegmentation(segmenter, image,
			extensionPoints.getCachedSegmentationImageFactory());
	}

	public List<String> labels() {
		return labels;
	}

	public Interval interval() {
		return new FinalInterval(Intervals.dimensionsAsLongArray(model.imageForSegmentation().get()));
	}

	public List<ARGBType> colors() {
		return colors;
	}

	public Notifier segmentationChangedListeners() {
		return listeners;
	}

	public boolean hasResults() {
		return hasResults;
	}
}
