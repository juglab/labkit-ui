
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.properties.DefaultProperty;
import net.imglib2.labkit.utils.properties.Property;
import net.imglib2.labkit.utils.ParametricNotifier;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A model that represents an image with an overlaid {@link Labeling}.
 * <p>
 * Serves as a model to {@link net.imglib2.labkit.LabelingComponent} and is an
 * important part of {@link SegmentationModel}.
 */
public class ImageLabelingModel implements LabelingModel {

	private final AffineTransform3D labelTransformation = new AffineTransform3D();

	private Property<Labeling> labelingProperty;

	private ParametricNotifier<Interval> dataChangedNotifier =
		new ParametricNotifier<>();

	private Property<Label> selectedLabelProperty;

	private Property<Boolean> imageVisibility = new DefaultProperty<>(true);

	private Property<Boolean> labelingVisibility = new DefaultProperty<>(true);

	private final boolean isTimeSeries;

	private final TransformationModel transformationModel;

	private final Property<BdvShowable> showable;

	private String defaultFileName;

	private final Property<ImgPlus<?>> imageForSegmentation;

	public ImageLabelingModel(InputImage inputImage) {
		ImgPlus<?> image = inputImage.imageForSegmentation();
		ImgPlus<?> firstChannel = ImgPlusViewsOld.hyperSlice(image, Axes.CHANNEL, 0);
		Interval intervalWithoutChannels = new FinalInterval(firstChannel);
		Labeling labeling = Labeling.createEmpty(Arrays.asList("background", "foreground"),
			intervalWithoutChannels);
		this.showable = new DefaultProperty<>(inputImage.showable());
		this.labelingProperty = new DefaultProperty<>(labeling);
		this.imageForSegmentation = new DefaultProperty<>(image);
		this.labelingProperty.notifier().addListener(this::labelingReplacedEvent);
		updateLabelTransform();
		Label anyLabel = labeling.getLabels().stream().findAny().orElse(null);
		this.selectedLabelProperty = new DefaultProperty<>(anyLabel);
		this.isTimeSeries = ImgPlusViewsOld.hasAxis(image, Axes.TIME);
		this.transformationModel = new TransformationModel(isTimeSeries);
	}

	private void updateLabelTransform() {
		labelTransformation.set(multiply(showable.get().transformation(), getScaling(
			showable.get().interval(), labelingProperty.get().interval())));
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
		Label selectedLabel = selectedLabelProperty.get();
		List<Label> labels = labelingProperty.get().getLabels();
		if (!labels.contains(selectedLabel)) selectedLabelProperty.set(labels
			.isEmpty() ? null : labels.get(0));
	}

	public Property<BdvShowable> showable() {
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
	public Property<Labeling> labeling() {
		return labelingProperty;
	}

	@Override
	public Property<Label> selectedLabel() {
		return selectedLabelProperty;
	}

	@Override
	public ParametricNotifier<Interval> dataChangedNotifier() {
		return dataChangedNotifier;
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	public Property<Boolean> imageVisibility() {
		return imageVisibility;
	}

	@Override
	public Property<Boolean> labelingVisibility() {
		return labelingVisibility;
	}

	@Override
	public TransformationModel transformationModel() {
		return transformationModel;
	}

	public Property<ImgPlus<?>> imageForSegmentation() {
		return imageForSegmentation;
	}

	public void setDefaultFileName(String defaultFileName) {
		this.defaultFileName = defaultFileName;
	}

	public Dimensions spatialDimensions() {
		Interval interval = labelingProperty.get().interval();
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
