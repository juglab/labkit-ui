
package sc.fiji.labkit.ui.segmentation;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.ui.bdv.BdvLayer;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.models.DefaultHolder;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.MappedHolder;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationResultsModel;
import sc.fiji.labkit.ui.utils.ParametricNotifier;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;

/**
 * A {@link BdvLayer} that lazily shows the result of a {@link Segmenter}.
 */
public class PredictionLayer implements BdvLayer {

	private final Holder<SegmentationResultsModel> model;
	private final AffineTransform3D transformation;
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
			segmentationModel.segmenterList().segmentationVisibility(),
			imageLabelingModel.labelTransformation());
	}

	private PredictionLayer(
		Holder<SegmentationResultsModel> model,
		Holder<Boolean> visibility,
		AffineTransform3D transformation)
	{
		this.model = model;
		this.transformation = transformation;
		this.showable = new DefaultHolder<>(null);
		this.visibility = visibility;
		model.notifier().addWeakListener(classifierChanged);
		registerListener(model.get());
		classifierChanged();
	}

	/**
	 * Makes this PredictionLayer listen to the given SegmentationResultsModel
	 *
	 * @param segmenter
	 */
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
			showable.set(BdvShowable.wrap(coloredVolatileView(results), transformation));
		else
			showable.set(null);
		listeners.notifyListeners(null);
	}

	private RandomAccessibleInterval<VolatileARGBType> coloredVolatileView(
		SegmentationResultsModel selected)
	{
		ARGBType[] colors = selected.colors().toArray(new ARGBType[0]);
		return mapColors(colors, wrapAsVolatile(selected.segmentation()));
	}

	private RandomAccessibleInterval<VolatileShortType> wrapAsVolatile(
		RandomAccessibleInterval<ShortType> image)
	{
		try {
			return VolatileViews.wrapAsVolatile(image, queue);
		}
		catch (IllegalArgumentException e) {
			// This happens when image isn't some sort of CachedCellImg.
			return Converters.convert(image, (i, o) -> o.set(i.get()), new VolatileShortType());
		}
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
