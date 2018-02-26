package net.imglib2.labkit.models;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ImageLabelingModel implements LabelingModel
{

	private final double scaling;

	private ColorMapProvider colorProvider;

	private Holder< Labeling > labelingHolder;

	private Notifier< Runnable > dataChangedNotifier = new Notifier<>();

	private Holder< String > selectedLabelHolder;

	private final boolean isTimeSeries;

	private final TransformationModel transformationModel = new TransformationModel();

	private BdvShowable showable;

	public ImageLabelingModel( RandomAccessibleInterval< ? extends NumericType< ? > > image, double scaling, Labeling labeling, boolean isTimeSeries )
	{
		this(BdvShowable.wrap( image ), scaling, labeling, isTimeSeries );
	}

	public ImageLabelingModel( BdvShowable showable, double scaling, Labeling labeling, boolean isTimeSeries )
	{
		this.showable = showable;
		this.scaling = scaling;
		this.labelingHolder = new CheckedHolder(labeling);
		this.labelingHolder.notifier().add(this::labelingReplacedEvent);
		this.selectedLabelHolder = new DefaultHolder<>(labeling.getLabels().stream().findAny().orElse(""));
		this.isTimeSeries = isTimeSeries;
		colorProvider = new ColorMapProvider(labelingHolder);
	}

	private void labelingReplacedEvent( Labeling labeling )
	{
		String selectedLabel = selectedLabelHolder.get();
		List< String > labels = labelingHolder.get().getLabels();
		if ( ! labels.contains( selectedLabel ) )
			selectedLabelHolder.set( labels.isEmpty() ? null : labels.get( 0 ) );
	}

	public BdvShowable showable() {
		return showable;
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

	@Override
	public boolean isTimeSeries()
	{
		return isTimeSeries;
	}

	public TransformationModel transformationModel() { return transformationModel; }

	public Dimensions spatialDimensions()
	{
		Interval interval = labelingHolder.get().interval();
		int n = interval.numDimensions() - (isTimeSeries() ? 1 : 0);
		return new FinalDimensions(IntStream.range(0, n).mapToLong( interval::dimension).toArray());
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
