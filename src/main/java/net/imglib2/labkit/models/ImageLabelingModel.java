package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;

import java.util.List;
import java.util.function.Consumer;

public class ImageLabelingModel implements LabelingModel {

	final RandomAccessibleInterval<? extends NumericType<?>> rawData;

	private final double scaling;

	private ColorMapProvider colorProvider;

	private Holder<Labeling> labelingHolder;

	private Notifier<Runnable> dataChangedNotifier = new Notifier<>();

	private Holder<String> selectedLabelHolder;

	public ImageLabelingModel(RandomAccessibleInterval<? extends NumericType<?>> image, double scaling, Labeling labeling) {
		this.rawData = image;
		this.scaling = scaling;
		this.labelingHolder = new CheckedHolder(labeling);
		this.labelingHolder.notifier().add(this::labelingReplacedEvent);
		this.selectedLabelHolder = new DefaultHolder<>(labeling.getLabels().stream().findAny().orElse(""));
		colorProvider = new ColorMapProvider(labelingHolder);
	}

	private void labelingReplacedEvent( Labeling labeling )
	{
		String selectedLabel = selectedLabelHolder.get();
		List< String > labels = labelingHolder.get().getLabels();
		if ( ! labels.contains( selectedLabel ) )
			selectedLabelHolder.set( labels.isEmpty() ? null : labels.get( 0 ) );
	}

	public RandomAccessibleInterval<? extends NumericType<?>> image() {
		return rawData;
	}

	public double scaling() {
		return scaling;
	}

	// -- LabelingModel methods --

	@Override
	public ColorMapProvider colorMapProvider() {
		return colorProvider;
	}

	@Override
	public Holder<Labeling> labeling() {
		return labelingHolder;
	}

	@Override
	public Holder<String> selectedLabel() { return selectedLabelHolder; }

	@Override
	public Notifier<Runnable> dataChangedNotifier() {
		return dataChangedNotifier;
	}

	private static class CheckedHolder implements Holder<Labeling> {

		Notifier<Consumer<Labeling>> notifier = new Notifier<>();

		Labeling value;

		CheckedHolder(Labeling value) {
			this.value = value;
		}

		@Override
		public void set(Labeling value) {
			if(! Intervals.equals(value, this.value))
				throw new IllegalArgumentException();
			this.value = value;
			notifier.forEach(listener -> listener.accept(value));
		}

		@Override
		public Labeling get() {
			return value;
		}

		@Override
		public Notifier<Consumer<Labeling>> notifier() {
			return notifier;
		}
	}
}
