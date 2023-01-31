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

package sc.fiji.labkit.ui.segmentation.weka;

import gnu.trove.list.array.TLongArrayList;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.display.imagej.ImgPlusViews;
import org.scijava.prefs.PrefService;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labelings;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.utils.LabkitUtils;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labkit.ui.utils.sparse.SparseRandomAccessIntType;
import sc.fiji.labkit.pixel_classification.classification.Training;
import sc.fiji.labkit.pixel_classification.gson.GsonUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.calculator.FeatureCalculator;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.ChannelSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
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

// TODO: rename to PixelClassification
public class TrainableSegmentationSegmenter implements Segmenter {

	private final Context context;

	private boolean useGpu;

	private FeatureSettings featureSettings;

	private sc.fiji.labkit.pixel_classification.classification.Segmenter segmenter;

	public TrainableSegmentationSegmenter(Context context) {
		this.context = Objects.requireNonNull(context);
		this.useGpu = getUseGpuPreference();
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
			boolean newUseGpu = dialog.useGpu();
			if (this.useGpu != newUseGpu)
				setUseGpuPreference(newUseGpu);
			setUseGpu(newUseGpu);
		}
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> labels)
	{
		if (ImgPlusViewsOld.hasAxis(image, Axes.TIME))
			applyOnSlices(this::segment, image, labels, image.dimensionIndex(Axes.TIME), labels
				.numDimensions() - 1);
		else if (ImgPlusViewsOld.hasAxis(image, Axes.Z) && is2D())
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
		else if (ImgPlusViewsOld.hasAxis(image, Axes.Z) && is2D())
			applyOnSlices(this::predict, image, prediction, image.dimensionIndex(Axes.Z), prediction
				.numDimensions() - 2);
		else
			segmenter.predict(prediction, Views.extendBorder(image));
	}

	private boolean is2D() {
		return segmenter.features().settings().globals().numDimensions() == 2;
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		try {
			initFeatureSettings(trainingData);
			List<String> classes = collectLabels(trainingData.stream().map(Pair::getB)
				.collect(Collectors.toList()));
			sc.fiji.labkit.pixel_classification.classification.Segmenter segmenter =
				new sc.fiji.labkit.pixel_classification.classification.Segmenter(context,
					classes, featureSettings, new FastRandomForest());
			segmenter.setUseGpu(useGpu);
			Training training = segmenter.training();
			for (Pair<ImgPlus<?>, Labeling> pair : trainingData)
				trainPair(training, classes, pair, segmenter);
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

	private void trainPair(Training training, List<String> classes, Pair<ImgPlus<?>, Labeling> pair,
		sc.fiji.labkit.pixel_classification.classification.Segmenter segmenter)
	{
		ImgPlus<?> image = pair.getA();
		Labeling labeling = pair.getB();
		checkMatchingSize(image, labeling);
		trainStack(training, classes, labeling, image, segmenter.features());
	}

	private void checkMatchingSize(ImgPlus<?> image, Labeling labeling) {
		long[] labelingSize = labeling.interval().dimensionsAsLongArray();
		long[] imageSize = getImageSizeXYZTnoChannel(image);
		boolean sizeMatches = !Arrays.equals(labelingSize, imageSize);
		if (sizeMatches) {
			String message = "Error: Pixel classifier cannot be trained.\n" +
				"The size of the image and the labeling don't match:\n" +
				"- image size: " + Arrays.toString(imageSize) + "\n" +
				"- labeling size: " + Arrays.toString(labelingSize);
			throw new CancellationException(message);
		}
	}

	private long[] getImageSizeXYZTnoChannel(ImgPlus<?> image) {
		TLongArrayList size = new TLongArrayList();
		size.add(ImgPlusViewsOld.getDimension(image, Axes.X));
		size.add(ImgPlusViewsOld.getDimension(image, Axes.Y));
		if (ImgPlusViewsOld.hasAxis(image, Axes.Z))
			size.add(ImgPlusViewsOld.getDimension(image, Axes.Z));
		if (ImgPlusViewsOld.hasAxis(image, Axes.TIME))
			size.add(ImgPlusViewsOld.getDimension(image, Axes.TIME));
		return size.toArray();
	}

	@Override
	public void setUseGpu(boolean useGpu) {
		this.useGpu = useGpu;
		if (segmenter != null)
			segmenter.setUseGpu(this.useGpu);
	}

	private static List<String> collectLabels(
		List<? extends Labeling> labelings)
	{
		return labelings.stream()
			.flatMap(labeling -> labeling.getLabels().stream())
			.map(Label::name)
			.distinct()
			.collect(Collectors.toList());
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
		ImgPlus<?> image, FeatureCalculator featuresCalculator)
	{
		SparseRandomAccessIntType classIndices = getClassIndices(labeling, classes);
		if (classIndices.sparsityPattern().size() == 0)
			return;
		DiskCachedCellImg<FloatType, ?> cachedFeatureBlock = cachedFeatureBlock(featuresCalculator,
			image);
		try {
			RandomAccessible<? extends Composite<FloatType>> features = Views.collapse(
				cachedFeatureBlock);
			addSamples(training, classIndices, features);
		}
		finally {
			cachedFeatureBlock.shutdown();
		}
	}

	private DiskCachedCellImg<FloatType, ?> cachedFeatureBlock(FeatureCalculator feature,
		ImgPlus<?> image)
	{
		int count = feature.count();
		if (count <= 0) throw new IllegalArgumentException();
		long[] dimensions = Intervals.dimensionsAsLongArray(feature.outputIntervalFromInput(image));
		dimensions = LabkitUtils.extend(dimensions, count);
		int[] cellDimensions = suggestCellSize(image);
		cellDimensions = LabkitUtils.extend(cellDimensions, count);
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions
			.options().cellDimensions(cellDimensions).dirtyAccesses(false);
		final DiskCachedCellImgFactory<FloatType> featureFactory =
			new DiskCachedCellImgFactory<>(new FloatType(), featureOpts);
		RandomAccessible<?> input = Views.extendBorder(image);
		CellLoader<FloatType> loader = target -> feature.apply(input, target);
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
		segmenter = sc.fiji.labkit.pixel_classification.classification.Segmenter
			.fromJson(context, GsonUtils.read(path));
		segmenter.setUseGpu(useGpu);
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
		return cellDimension;
	}

	@Override
	public boolean requiresFixedCellSize() {
		return useGpu;
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
			GroupedFeatures.hessian());
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

	// -- Helper methods --

	private boolean getUseGpuPreference() {
		PrefService prefService = context.service(PrefService.class);
		return prefService.getBoolean(TrainableSegmentationSegmenter.class, "USE_GPU", false);
	}

	private void setUseGpuPreference(boolean useGpu) {
		setUseGpuPreference(context, useGpu);
	}

	public static void setUseGpuPreference(Context context, boolean useGpu) {
		PrefService prefService = context.service(PrefService.class);
		prefService.put(TrainableSegmentationSegmenter.class, "USE_GPU", useGpu);
	}
}
