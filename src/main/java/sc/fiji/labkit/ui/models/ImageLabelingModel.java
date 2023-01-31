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
import net.imagej.axis.Axes;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.utils.ParametricNotifier;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A model that represents an image with an overlaid {@link Labeling}.
 * <p>
 * Serves as a model to {@link sc.fiji.labkit.ui.LabelingComponent} and is an
 * important part of {@link SegmentationModel}.
 */
public class ImageLabelingModel implements LabelingModel {

	private final AffineTransform3D labelTransformation = new AffineTransform3D();

	private Holder<Labeling> labelingHolder;

	private ParametricNotifier<Interval> dataChangedNotifier =
		new ParametricNotifier<>();

	private Holder<Label> selectedLabelHolder;

	private Holder<Boolean> imageVisibility = new DefaultHolder<>(true);

	private Holder<Boolean> labelingVisibility = new DefaultHolder<>(true);

	private final boolean isTimeSeries;

	private final TransformationModel transformationModel;

	private final Holder<BdvShowable> showable;

	private String defaultFileName;

	private final Holder<ImgPlus<?>> imageForSegmentation;

	public ImageLabelingModel(InputImage inputImage) {
		ImgPlus<?> image = inputImage.imageForSegmentation();
		ImgPlus<?> firstChannel = ImgPlusViewsOld.hyperSlice(image, Axes.CHANNEL, 0);
		Interval intervalWithoutChannels = new FinalInterval(firstChannel);
		Labeling labeling = Labeling.createEmpty(Arrays.asList("background", "foreground"),
			intervalWithoutChannels);
		this.showable = new DefaultHolder<>(inputImage.showable());
		this.labelingHolder = new DefaultHolder<>(labeling);
		this.imageForSegmentation = new DefaultHolder<>(image);
		this.labelingHolder.notifier().addListener(this::labelingReplacedEvent);
		updateLabelTransform();
		Label anyLabel = labeling.getLabels().stream().findAny().orElse(null);
		this.selectedLabelHolder = new DefaultHolder<>(anyLabel);
		this.isTimeSeries = ImgPlusViewsOld.hasAxis(image, Axes.TIME);
		this.transformationModel = new TransformationModel(isTimeSeries);
		this.defaultFileName = inputImage.getDefaultLabelingFilename();
	}

	private void updateLabelTransform() {
		labelTransformation.set(multiply(showable.get().transformation(), getScaling(
			showable.get().interval(), labelingHolder.get().interval())));
	}

	private AffineTransform3D multiply(AffineTransform3D transformation,
		AffineTransform3D scaling)
	{
		AffineTransform3D result = new AffineTransform3D();
		result.set(transformation);
		result.concatenate(scaling);
		return result;
	}

	private void labelingReplacedEvent() {
		updateLabelTransform();
		Label selectedLabel = selectedLabelHolder.get();
		List<Label> labels = labelingHolder.get().getLabels();
		if (!labels.contains(selectedLabel)) selectedLabelHolder.set(labels
			.isEmpty() ? null : labels.get(0));
	}

	public Holder<BdvShowable> showable() {
		return showable;
	}

	// -- LabelingModel methods --

	@Override
	public AffineTransform3D labelTransformation() {
		return labelTransformation;
	}

	@Override
	public String defaultFileName() {
		return defaultFileName;
	}

	@Override
	public Holder<Labeling> labeling() {
		return labelingHolder;
	}

	@Override
	public Holder<Label> selectedLabel() {
		return selectedLabelHolder;
	}

	@Override
	public ParametricNotifier<Interval> dataChangedNotifier() {
		return dataChangedNotifier;
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	public Holder<Boolean> imageVisibility() {
		return imageVisibility;
	}

	@Override
	public Holder<Boolean> labelingVisibility() {
		return labelingVisibility;
	}

	@Override
	public TransformationModel transformationModel() {
		return transformationModel;
	}

	public Holder<ImgPlus<?>> imageForSegmentation() {
		return imageForSegmentation;
	}

	public Dimensions spatialDimensions() {
		Interval interval = labelingHolder.get().interval();
		int n = interval.numDimensions() - (isTimeSeries() ? 1 : 0);
		return new FinalDimensions(IntStream.range(0, n).mapToLong(
			interval::dimension).toArray());
	}

	private AffineTransform3D getScaling(Interval inputImage,
		Interval initialLabeling)
	{
		long[] dimensionsA = get3dDimensions(inputImage);
		long[] dimensionsB = get3dDimensions(initialLabeling);
		double[] values = IntStream.range(0, 3).mapToDouble(
			i -> (double) dimensionsA[i] / (double) dimensionsB[i]).toArray();
		AffineTransform3D affineTransform3D = new AffineTransform3D();
		affineTransform3D.set(values[0], 0.0, 0.0, 0.0, 0.0, values[1], 0.0, 0.0,
			0.0, 0.0, values[2], 0.0);
		return affineTransform3D;
	}

	private long[] get3dDimensions(Interval interval) {
		long[] result = new long[3];
		int n = interval.numDimensions();
		for (int i = 0; i < n & i < 3; i++)
			result[i] = interval.dimension(i);
		for (int i = n; i < 3; i++)
			result[i] = 1;
		return result;
	}

	@Override
	public String toString() {
		return imageForSegmentation.get().getName();
	}
}
