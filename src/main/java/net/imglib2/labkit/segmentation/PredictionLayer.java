
package net.imglib2.labkit.segmentation;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.MappedHolder;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.utils.ParametricNotifier;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;

/**
 * A {@link BdvLayer} that lazily shows the result of a {@link Segmenter}.
 */
public class PredictionLayer implements BdvLayer {

	private final Holder<SegmentationResultsModel> model;
	private final SharedQueue queue = new SharedQueue(Runtime.getRuntime()
		.availableProcessors());
	private final Holder<Boolean> visibility;
	private final Runnable classifierChanged = this::classifierChanged;
	private ParametricNotifier<Interval> listeners = new ParametricNotifier<>();
	private DefaultHolder<BdvShowable> showable;
	private final Runnable onTrainingCompleted = this::onTrainingCompleted;

	private SegmentationResultsModel segmenter;

	public static PredictionLayer createPredictionLayer(SegmentationModel segmentationModel) {
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		return new PredictionLayer(
			new MappedHolder<>(segmentationModel.segmenterList().selectedSegmenter(), si -> si == null
				? null : si.results(imageLabelingModel)),
			segmentationModel.segmenterList().segmentationVisibility());
	}

	private PredictionLayer(
		Holder<SegmentationResultsModel> model,
		Holder<Boolean> visibility)
	{
		this.model = model;
		this.showable = new DefaultHolder<>(null);
		this.visibility = visibility;
		model.notifier().addWeakListener(classifierChanged);
		registerListener(model.get());
		classifierChanged();
	}

	private void registerListener(SegmentationResultsModel segmenter) {
		if (segmenter == this.segmenter)
			return;
		if (this.segmenter != null)
			this.segmenter.segmentationChangedListeners().removeWeakListener(onTrainingCompleted);
		this.segmenter = segmenter;
		if (this.segmenter != null)
			this.segmenter.segmentationChangedListeners().addWeakListener(onTrainingCompleted);
	}

	private void onTrainingCompleted() {
		if (model.get() == segmenter) {
			classifierChanged();
			visibility.set(true);
		}
	}

	private void classifierChanged() {
		SegmentationResultsModel results = model.get();
		registerListener(results);
		boolean hasResult = results != null && results.hasResults();
		if (hasResult)
			showable.set(BdvShowable.wrap(coloredVolatileView(results)));
		else
			showable.set(null);
		listeners.notifyListeners(null);
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
	public Holder<BdvShowable> image() {
		return showable;
	}

	@Override
	public ParametricNotifier<Interval> listeners() {
		return listeners;
	}

	@Override
	public Holder<Boolean> visibility() {
		return visibility;
	}

	@Override
	public String title() {
		return "Segmentation";
	}
}
