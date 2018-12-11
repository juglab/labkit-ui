
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ImageLabelingModel implements LabelingModel {

	private final AffineTransform3D labelTransformation = new AffineTransform3D();

	private Holder<Labeling> labelingHolder = new DefaultHolder<>(null);

	private Notifier<Runnable> dataChangedNotifier = new Notifier<>();

	private Holder<Label> selectedLabelHolder = new DefaultHolder<>(null);

	private Holder<Boolean> imageVisibility = new DefaultHolder<>(true);

	private Holder<Boolean> labelingVisibility = new DefaultHolder<>(true);

	private final boolean isTimeSeries;

	private final TransformationModel transformationModel =
		new TransformationModel();

	private Holder<BdvShowable> showable = new DefaultHolder<>(null);

	private final Holder<String> labelingFileName = new DefaultHolder<>("");

	public ImageLabelingModel(boolean isTimeSeries) {
		this.labelingHolder.notifier().add(this::labelingReplacedEvent);
		this.isTimeSeries = isTimeSeries;
	}

	private void labelingReplacedEvent(Labeling labeling) {
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
	public Holder<String> labelingFileName() {
		return labelingFileName;
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
	public Notifier<Runnable> dataChangedNotifier() {
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

	public TransformationModel transformationModel() {
		return transformationModel;
	}

	public Dimensions spatialDimensions() {
		Interval interval = labelingHolder.get().interval();
		int n = interval.numDimensions() - (isTimeSeries() ? 1 : 0);
		return new FinalDimensions(IntStream.range(0, n).mapToLong(
			interval::dimension).toArray());
	}

	public void setImage(RandomAccessibleInterval<?> image) {
		this.showable.set(BdvShowable.wrap(image));
	}

	public void setImage(ImgPlus<?> image) {
		this.showable.set(BdvShowable.wrap(image));
	}

	public void createEmptyLabeling() {
		final BdvShowable showable = this.showable().get();
		this.labeling().set(Labeling.createEmpty(Collections.emptyList(), showable
			.interval()));
		this.labelTransformation.set(showable.transformation().copy());
	}

	public void resetTransformation() {
		final BdvShowable bdvShowable = showable().get();
		transformationModel.transformToShowInterval(bdvShowable.interval(),
			bdvShowable.transformation());
	}
}
