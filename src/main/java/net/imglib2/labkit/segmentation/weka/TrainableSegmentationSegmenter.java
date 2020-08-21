
package net.imglib2.labkit.segmentation.weka;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labelings;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.trainable_segmentation.classification.Training;
import net.imglib2.trainable_segmentation.gson.GsonUtils;
import net.imglib2.trainable_segmentation.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmentation.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmentation.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmentation.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.Context;
import weka.core.WekaException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrainableSegmentationSegmenter implements Segmenter {

	private final Context context;

	private boolean useGpu;

	private FeatureSettings featureSettings;

	private net.imglib2.trainable_segmentation.classification.Segmenter segmenter;

	public TrainableSegmentationSegmenter(Context context) {
		this.context = Objects.requireNonNull(context);
		this.useGpu = false;
		this.segmenter = null;
		this.featureSettings = null;
	}

	@Override
	public List<String> classNames() {
		return segmenter.classNames();
	}

	@Override
	public void editSettings(JFrame dialogParent, List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		initFeatureSettings(trainingData);
		TrainableSegmentationSettingsDialog dialog =
			new TrainableSegmentationSettingsDialog(context, dialogParent,
				useGpu, featureSettings);
		dialog.show();
		if (dialog.okClicked()) {
			featureSettings = dialog.featureSettings();
			useGpu = dialog.useGpu();
			if (segmenter != null)
				segmenter.setUseGpu(useGpu);
		}
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> labels)
	{
		if (ImgPlusViewsOld.hasAxis(image, Axes.TIME))
			applyOnSlices(this::segment, image, labels, image.dimensionIndex(Axes.TIME), labels
				.numDimensions() - 1);
		else if (ImgPlusViewsOld.hasAxis(image, Axes.Z))
			applyOnSlices(this::segment, image, labels, image.dimensionIndex(Axes.Z), labels
				.numDimensions() - 1);
		else
			segmenter.segment(labels, Views.extendBorder(image));
	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> prediction)
	{
		if (ImgPlusViewsOld.hasAxis(image, Axes.TIME))
			applyOnSlices(this::predict, image, prediction, image.dimensionIndex(Axes.TIME), prediction
				.numDimensions() - 2);
		else if (ImgPlusViewsOld.hasAxis(image, Axes.Z))
			applyOnSlices(this::predict, image, prediction, image.dimensionIndex(Axes.Z), prediction
				.numDimensions() - 2);
		else
			segmenter.predict(prediction, Views.extendBorder(image));
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		try {
			initFeatureSettings(trainingData);
			List<String> classes = collectLabels(trainingData.stream().map(Pair::getB)
				.collect(Collectors.toList()));
			net.imglib2.trainable_segmentation.classification.Segmenter segmenter =
				new net.imglib2.trainable_segmentation.classification.Segmenter(context,
					classes, featureSettings, new FastRandomForest());
			segmenter.setUseGpu(useGpu);
			Training training = segmenter.training();
			for (Pair<ImgPlus<?>, Labeling> pair : trainingData)
				trainStack(training, classes, pair.getB(), pair.getA(), segmenter.features());
			training.train();
			this.segmenter = segmenter;
		}
		catch (RuntimeException e) {
			Throwable cause = e.getCause();
			if (cause instanceof WekaException && cause.getMessage().contains(
				"Not enough training instances")) throw new CancellationException(
					"The training requires some labeled regions.");
			throw e;
		}
	}

	private static List<String> collectLabels(
		List<? extends Labeling> labelings)
	{
		return new ArrayList<>(labelings.stream().flatMap(labeling -> labeling
			.getLabels().stream()).map(Label::name).collect(Collectors.toSet()));
	}

	private void trainStack(Training training, List<String> classes, Labeling labeling,
		ImgPlus<?> image, FeatureCalculator featuresCalculator)
	{
		if (ImgPlusViewsOld.hasAxis(image, Axes.TIME)) {
			List<ImgPlus<?>> imageSlices = ImgPlusViewsOld.hyperSlices(image, Axes.TIME);
			List<Labeling> labelSlices = Labelings.slices(labeling);
			for (int i = 0; i < imageSlices.size(); i++) {
				trainStack(training, classes, labelSlices.get(i), imageSlices.get(i), featuresCalculator);
			}
		}
		else if (ImgPlusViewsOld.hasAxis(image, Axes.Z) && featureSettings.globals()
			.numDimensions() == 2)
		{
			List<ImgPlus<?>> imageSlices = ImgPlusViewsOld.hyperSlices(image, Axes.Z);
			List<Labeling> labelSlices = Labelings.slices(labeling);
			for (int i = 0; i < imageSlices.size(); i++) {
				trainStack(training, classes, labelSlices.get(i), imageSlices.get(i), featuresCalculator);
			}
		}
		else {
			trainFrame(training, classes, labeling, image, featuresCalculator);
		}
	}

	private void trainFrame(Training training, List<String> classes, Labeling labeling,
		RandomAccessibleInterval<?> image, FeatureCalculator featuresCalculator)
	{
		SparseRandomAccessIntType classIndices = getClassIndices(labeling, classes);
		RandomAccessible<? extends Composite<FloatType>> features = Views.collapse(
			cachedFeatureBlock(featuresCalculator, image));
		addSamples(training, classIndices, features);
	}

	// TODO: caching the Feature Stack while training could be part of
	// imglib2-trainable-segmentation
	private static Img<FloatType> cachedFeatureBlock(FeatureCalculator feature,
		RandomAccessibleInterval<?> image)
	{
		return cachedFeatureBlock(feature, Views.extendBorder(image),
			suggestGrid(feature.outputIntervalFromInput(image)));
	}

	private static CellGrid suggestGrid(Interval interval) {
		long[] imageDimensions = Intervals.dimensionsAsLongArray(interval);
		int[] cellDimensions = interval.numDimensions() == 2 ? new int[] { 128, 128 } : new int[] { 64,
			64, 64 };
		return new CellGrid(imageDimensions, cellDimensions);
	}

	private static Img<FloatType> cachedFeatureBlock(FeatureCalculator feature,
		RandomAccessible<?> extendedOriginal, CellGrid grid)
	{
		int count = feature.count();
		if (count <= 0) throw new IllegalArgumentException();
		long[] dimensions = LabkitUtils.extend(grid.getImgDimensions(), count);
		int[] cellDimensions = LabkitUtils.extend(new int[grid.numDimensions()],
			count);
		grid.cellDimensions(cellDimensions);
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions
			.options().cellDimensions(cellDimensions).dirtyAccesses(false);
		final DiskCachedCellImgFactory<FloatType> featureFactory =
			new DiskCachedCellImgFactory<>(new FloatType(), featureOpts);
		CellLoader<FloatType> loader = target -> feature.apply(extendedOriginal,
			target);
		return featureFactory.create(dimensions, loader);
	}

	private void addSamples(Training training,
		SparseRandomAccessIntType classIndices,
		RandomAccessible<? extends Composite<FloatType>> features)
	{
		Cursor<IntType> classIndicesCursor = classIndices.sparseCursor();
		RandomAccess<? extends Composite<? extends RealType<?>>> ra = features
			.randomAccess();
		while (classIndicesCursor.hasNext()) {
			int classIndex = classIndicesCursor.next().get();
			ra.setPosition(classIndicesCursor);
			training.add(ra.get(), classIndex);
		}
	}

	private SparseRandomAccessIntType getClassIndices(Labeling labeling,
		List<String> classes)
	{
		SparseRandomAccessIntType result = new SparseRandomAccessIntType(labeling,
			-1);
		Map<Set<Label>, Integer> classIndices = new HashMap<>();
		Function<Set<Label>, Integer> compute = set -> set.stream().mapToInt(
			label -> classes.indexOf(label.name())).filter(i -> i >= 0).min().orElse(
				-1);
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<LabelingType<Label>> randomAccess = labeling.randomAccess();
		RandomAccess<IntType> out = result.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			randomAccess.setPosition(cursor);
			Set<Label> labels = randomAccess.get();
			if (labels.isEmpty()) continue;
			Integer classIndex = classIndices.computeIfAbsent(labels, compute);
			out.setPosition(cursor);
			out.get().set(classIndex);
		}
		return result;
	}

	@Override
	public boolean isTrained() {
		return segmenter != null;
	}

	@Override
	synchronized public void saveModel(final String path) {
		GsonUtils.write(segmenter.toJsonTree(), path);
	}

	@Override
	public void openModel(final String path) {
		segmenter = net.imglib2.trainable_segmentation.classification.Segmenter
			.fromJson(context, GsonUtils.read(path));
		featureSettings = segmenter.features().settings();
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		if (ImgPlusViewsOld.hasAxis(image, Axes.CHANNEL))
			image = ImgPlusViewsOld.hyperSlice(image, Axes.CHANNEL, 0);
		int spacialDimensions = ImgPlusViewsOld.numberOfSpatialDimensions(image);
		int cellSize = spacialDimensions <= 2 ? 128 : 32;
		if (useGpu)
			cellSize *= 2;
		int[] cellDimension = new int[image.numDimensions()];
		for (int i = 0; i < cellDimension.length; i++) {
			cellDimension[i] = image.axis(i).type().isSpatial() ? cellSize : 1;
		}
		Arrays.fill(cellDimension, cellSize);
		return cellDimension;
	}

	// -- Helper methods --

	private static List<Double> getPixelSize(ImgPlus<?> image) {
		List<Double> pixelSize = new ArrayList<>();
		double x = getPixelSize(image, Axes.X);
		double y = getPixelSize(image, Axes.Y);
		pixelSize.add(1.0);
		pixelSize.add(y / x);
		if (ImgPlusViewsOld.hasAxis(image, Axes.Z)) {
			double z = getPixelSize(image, Axes.Z);
			pixelSize.add(z / x);
		}
		return pixelSize;
	}

	private static double getPixelSize(ImgPlus<?> image, AxisType axis) {
		double scale = image.averageScale(image.dimensionIndex(axis));
		return Double.isNaN(scale) || scale == 0 ? 1.0 : scale;
	}

	private void initFeatureSettings(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		if (this.featureSettings != null)
			return;
		final GlobalSettings globalSettings = initGlobalSettings(trainingData);
		this.featureSettings = new FeatureSettings(globalSettings,
			SingleFeatures.identity(),
			GroupedFeatures.gauss(),
			GroupedFeatures.differenceOfGaussians(),
			GroupedFeatures.gradient(),
			GroupedFeatures.laplacian(),
			GroupedFeatures.hessian(),
			GroupedFeatures.structureTensor());
	}

	private GlobalSettings initGlobalSettings(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		if (trainingData.isEmpty())
			return GlobalSettings.default2d().build();
		ImgPlus<?> image = trainingData.get(0).getA();
		final ChannelSetting channelSetting = getChannelSetting(image);
		return GlobalSettings.default2d()
			.dimensions(ImgPlusViewsOld.numberOfSpatialDimensions(image))
			.channels(channelSetting)
			.sigmaRange(1.0, 8.0)
			.pixelSize(getPixelSize(image))
			.build();
	}

	private static ChannelSetting getChannelSetting(ImgPlus<?> image) {
		if (ImgPlusViewsOld.hasAxis(image, Axes.CHANNEL))
			return ChannelSetting.multiple((int) ImgPlusViewsOld.getDimension(image, Axes.CHANNEL));
		return image.firstElement() instanceof ARGBType ? ChannelSetting.RGB : ChannelSetting.SINGLE;
	}

	private <T> void applyOnSlices(BiConsumer<ImgPlus<?>, RandomAccessibleInterval<T>> action,
		ImgPlus<?> image, RandomAccessibleInterval<T> target, int imageTimeAxis, int targetTimeAxis)
	{
		long min = target.min(targetTimeAxis);
		long max = target.max(targetTimeAxis);
		if (min < image.min(imageTimeAxis) || max > image.max(imageTimeAxis))
			throw new IllegalStateException("Last dimensions must fit.");
		for (long pos = min; pos <= max; pos++) {
			RandomAccessibleInterval<T> targetSlize = Views.hyperSlice(target, targetTimeAxis, pos);
			ImgPlus<?> imageSlice = ImgPlusViews.hyperSlice(Cast.unchecked(image), imageTimeAxis, pos);
			action.accept(imageSlice, targetSlize);
		}
	}

	public void setFeatureSettings(FeatureSettings featureSettings) {
		this.featureSettings = featureSettings;
	}
}
