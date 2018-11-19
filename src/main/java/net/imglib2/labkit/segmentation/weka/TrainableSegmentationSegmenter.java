
package net.imglib2.labkit.segmentation.weka;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.classification.Training;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.trainable_segmention.pixel_feature.calculator.FeatureCalculator;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.Context;
import weka.classifiers.AbstractClassifier;
import weka.core.WekaException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrainableSegmentationSegmenter implements Segmenter {

	private final Context context;

	private weka.classifiers.Classifier initialWekaClassifier;

	private FeatureSettings featureSettings;

	private net.imglib2.trainable_segmention.classification.Segmenter segmenter;

	private final Notifier<Runnable> listeners = new Notifier<>();

	@Override
	public Notifier<Runnable> trainingCompletedListeners() {
		return listeners;
	}

	@Override
	public List<String> classNames() {
		return segmenter.classNames();
	}

	@Override
	public void editSettings(JFrame dialogParent) {
		TrainableSegmentationSettingsDialog dialog =
			new TrainableSegmentationSettingsDialog(context, dialogParent,
				initialWekaClassifier, featureSettings);
		dialog.show();
		if (dialog.okClicked()) {
			featureSettings = dialog.featureSettings();
			initialWekaClassifier = dialog.wekaClassifier();
		}
	}

	public TrainableSegmentationSegmenter(Context context,
		InputImage inputImage)
	{
		GlobalSettings globalSettings = new GlobalSettings(inputImage
			.getChannelSetting(), inputImage.getSpatialDimensions(), 1.0, 16.0, 1.0);
		this.context = context;
		this.initialWekaClassifier = new FastRandomForest();
		this.featureSettings = new FeatureSettings(globalSettings, SingleFeatures
			.identity(), GroupedFeatures.gauss());
		this.segmenter = null;
	}

	public TrainableSegmentationSegmenter(Context context) {
		GlobalSettings globalSettings = GlobalSettings.default3dSettings();
		this.context = context;
		this.initialWekaClassifier = new FastRandomForest();
		this.featureSettings = new FeatureSettings(globalSettings, SingleFeatures
			.identity());
		this.segmenter = null;
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> labels)
	{
		segmenter.segment(labels, Views.extendBorder(image));
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends RealType<?>> prediction)
	{
		segmenter.predict(Views.collapse(prediction), Views.extendBorder(image));
	}

	@Override
	public void train(
		List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data)
	{
		try {
			List<String> classes = collectLabels(data.stream().map(Pair::getB)
				.collect(Collectors.toList()));
			weka.classifiers.Classifier wekaClassifier = RevampUtils.wrapException(
				() -> AbstractClassifier.makeCopy(this.initialWekaClassifier));
			OpEnvironment ops = context.service(OpService.class);
			net.imglib2.trainable_segmention.classification.Segmenter segmenter =
				new net.imglib2.trainable_segmention.classification.Segmenter(ops,
					classes, featureSettings, wekaClassifier);
			Training training = segmenter.training();
			for (Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> pair : data)
				train(training, classes, pair.getB(), pair.getA(), segmenter
					.features());
			training.train();
			this.segmenter = segmenter;
			listeners.forEach(Runnable::run);
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

	private void train(Training training, List<String> classes, Labeling labeling,
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
		return cachedFeatureBlock(feature, Views.extendBorder(image), LabkitUtils
			.suggestGrid(feature.outputIntervalFromInput(image), false));
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
			RevampUtils.slices(target));
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
		RandomAccess<Set<Label>> randomAccess = labeling.randomAccess();
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
		segmenter = net.imglib2.trainable_segmention.classification.Segmenter
			.fromJson(context.service(OpService.class), GsonUtils.read(path));
		featureSettings = segmenter.features().settings();
		listeners.forEach(Runnable::run);
	}
}
