
package net.imglib2.labkit.segmentation;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.RandomAccessibleContainer;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class PredictionLayer implements BdvLayer {

	private final Holder<? extends SegmentationItem> model;
	private final RandomAccessibleContainer<VolatileARGBType> segmentationContainer;
	private final SharedQueue queue = new SharedQueue(Runtime.getRuntime()
		.availableProcessors());
	private Notifier<Runnable> listeners = new Notifier<>();
	private final Notifier<Runnable> makeVisible = new Notifier<>();
	private RandomAccessibleInterval<? extends NumericType<?>> view;
	private AffineTransform3D transformation;
	private Set<Segmenter> alreadyRegistered = Collections.newSetFromMap(
		new WeakHashMap<>());

	public PredictionLayer(Holder<? extends SegmentationItem> model) {
		this.model = model;
		SegmentationResultsModel selected = model.get().results(); // don't use
																																// selected
																																// segmentation
																																// result for
																																// initialization
		this.segmentationContainer = new RandomAccessibleContainer<>(
			getEmptyPrediction(selected));
		this.transformation = selected.transformation();
		this.view = Views.interval(segmentationContainer, selected.interval());
		model.notifier().add(ignore -> classifierChanged());
		registerListener(model.get().segmenter());
	}

	private void registerListener(Segmenter segmenter) {
		if (alreadyRegistered.contains(segmenter)) return;
		alreadyRegistered.add(segmenter);
		segmenter.listeners().add(this::onTrainingFinished);
	}

	private void onTrainingFinished(Segmenter segmenter) {
		if (model.get().segmenter() == segmenter) {
			classifierChanged();
			makeVisible.forEach(Runnable::run);
		}
	}

	private RandomAccessible<VolatileARGBType> getEmptyPrediction(
		SegmentationResultsModel selected)
	{
		return ConstantUtils.constantRandomAccessible(new VolatileARGBType(0),
			selected.interval().numDimensions());
	}

	private static AffineTransform3D scaleTransformation(double scaling) {
		AffineTransform3D transformation = new AffineTransform3D();
		transformation.scale(scaling);
		return transformation;
	}

	private void classifierChanged() {
		SegmentationItem segmentationItem = model.get();
		registerListener(segmentationItem.segmenter());
		SegmentationResultsModel selected = segmentationItem.results();
		RandomAccessible<VolatileARGBType> source = selected.hasResults() ? Views
			.extendValue(coloredVolatileView(selected), new VolatileARGBType(0))
			: getEmptyPrediction(selected);
		segmentationContainer.setSource(source);
		listeners.forEach(Runnable::run);
	}

	private RandomAccessibleInterval<VolatileARGBType> coloredVolatileView(
		SegmentationResultsModel selected)
	{
		ARGBType[] colors = selected.colors().toArray(new ARGBType[0]);
		return mapColors(colors, VolatileViews.wrapAsVolatile(selected
			.segmentation(), queue));
	}

	private RandomAccessibleInterval<VolatileARGBType> mapColors(
		ARGBType[] colors, RandomAccessibleInterval<VolatileShortType> source)
	{
		final Converter<VolatileShortType, VolatileARGBType> conv = (input,
			output) -> {
			final boolean isValid = input.isValid();
			output.setValid(isValid);
			if (isValid) output.set(colors[input.get().get()].get());
		};

		return Converters.convert(source, conv, new VolatileARGBType());
	}

	@Override
	public BdvShowable image() {
		return BdvShowable.wrap(view, transformation);
	}

	@Override
	public Notifier<Runnable> listeners() {
		return listeners;
	}

	@Override
	public Notifier<Runnable> makeVisible() {
		return makeVisible;
	}

	@Override
	public String title() {
		return "Segmentation";
	}
}
