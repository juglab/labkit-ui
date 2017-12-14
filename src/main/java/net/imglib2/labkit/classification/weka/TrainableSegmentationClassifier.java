package net.imglib2.labkit.classification.weka;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ops.OpEnvironment;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.FeatureStack;
import net.imglib2.labkit.Notifier;
import net.imglib2.labkit.actions.SelectClassifier;
import net.imglib2.labkit.classification.Classifier;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.classification.Segmenter;
import net.imglib2.trainable_segmention.classification.Training;
import net.imglib2.trainable_segmention.gson.GsonUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import weka.classifiers.AbstractClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TrainableSegmentationClassifier
implements Classifier
{
	private final OpEnvironment ops;

	private weka.classifiers.Classifier initialWekaClassifier;

	private Segmenter classifier;

	private final Notifier< Listener > listeners = new Notifier<>();

	private boolean isTrained = false;

	@Override
	public Notifier<Listener> listeners() {
		return listeners;
	}

	@Override
	public FeatureSettings settings() {
		return classifier.settings();
	}

	@Override
	public List<String> classNames() {
		return classifier.classNames();
	}

	@Override
	public void editClassifier() {
		initialWekaClassifier = SelectClassifier.runStatic(null, initialWekaClassifier);
		reset(classifier.settings(), classifier.classNames());
	}

	@Override
	public void reset(FeatureSettings settings, List<String> classLabels) {
		weka.classifiers.Classifier wekaClassifier = RevampUtils.wrapException(() ->
				AbstractClassifier.makeCopy(this.initialWekaClassifier));
		reset(new Segmenter(ops, classLabels, settings, wekaClassifier));
	}

	private void reset(Segmenter classifier) {
		this.classifier = classifier;
		isTrained = false;
		listeners.forEach(l -> l.notify(this));
	}

	public TrainableSegmentationClassifier(OpEnvironment ops, weka.classifiers.Classifier initialWekaClassifier, final List<String> classLabels, FeatureSettings features)
	{
		this.ops = ops;
		this.initialWekaClassifier = initialWekaClassifier;
		reset(features, classLabels);
	}

	public TrainableSegmentationClassifier(OpEnvironment ops, Segmenter classifier)
	{
		this.ops = ops;
		this.initialWekaClassifier = new FastRandomForest();
		this.classifier = classifier;
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends IntegerType<?>> labels) {
		classifier.segment(labels, Views.extendBorder(image));
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image, RandomAccessibleInterval<? extends RealType<?>> prediction) {
		classifier.predict(Views.collapse(prediction), Views.extendBorder(image));
	}

	@Override
	public void train(List<? extends RandomAccessibleInterval<?>> images, List<? extends Labeling> labelings) {
		if(labelings.size() != images.size())
			throw new IllegalArgumentException();
		weka.classifiers.Classifier wekaClassifier = RevampUtils.wrapException(() ->
				AbstractClassifier.makeCopy(this.initialWekaClassifier));
		List<String> classes = collectLabels(labelings);
		classifier = new Segmenter(ops, classes, classifier.features(), wekaClassifier);
		Training training = classifier.training();
		for (int i = 0; i < images.size(); i++)
			train(training, classes, labelings.get(i), images.get(i));
		training.train();
		isTrained = true;
		listeners.forEach(l -> l.notify(this));
	}

	private void train(Training training, List<String> classes, Labeling labeling, RandomAccessibleInterval<?> image) {
		SparseRandomAccessIntType classIndices = getClassIndices(labeling, classes);
		RandomAccessible<? extends Composite<FloatType>> features =
				Views.collapse(FeatureStack.cachedFeatureBlock(classifier.features(), image));
		addSamples(training, classIndices, features);
	}

	private List<String> collectLabels(List<? extends Labeling> labelings) {
		return new ArrayList<>(labelings.stream().
				flatMap(labeling -> labeling.getLabels().stream())
				.collect(Collectors.toSet()));
	}

	private void addSamples(Training training, SparseRandomAccessIntType classIndices, RandomAccessible<? extends Composite<FloatType>> features) {
		Cursor<IntType> classIndicesCursor = classIndices.sparseCursor();
		RandomAccess<? extends Composite<? extends RealType<?>>> ra = features.randomAccess();
		while(classIndicesCursor.hasNext()) {
			int classIndex = classIndicesCursor.next().get();
			ra.setPosition(classIndicesCursor);
			training.add(ra.get(), classIndex);
		}
	}

	private SparseRandomAccessIntType getClassIndices(Labeling labeling, List<String> classes) {
		SparseRandomAccessIntType result = new SparseRandomAccessIntType(labeling, -1);
		Map<Set<String>, Integer> classIndices = new HashMap<>();
		Function<Set<String>, Integer> compute = set -> {
			for (int i = 0; i < classes.size(); i++)
				if(set.contains(classes.get(i)))
					return i;
			return -1;
		};
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<Set<String>> randomAccess = labeling.randomAccess();
		RandomAccess<IntType> out = result.randomAccess();
		while(cursor.hasNext()) {
			cursor.fwd();
			randomAccess.setPosition(cursor);
			Set<String> labels = randomAccess.get();
			if(labels.isEmpty()) continue;
			Integer classIndex = classIndices.computeIfAbsent(labels, compute);
			out.setPosition(cursor);
			out.get().set(classIndex);
		}
		return result;
	}

	@Override
	public boolean isTrained() {
		return isTrained;
	}

	@Override
	synchronized public void saveClassifier( final String path, final boolean overwrite ) throws Exception
	{
		GsonUtils.write(classifier.toJsonTree(), path);
	}

	@Override
	public void openClassifier( final String path ) throws Exception
	{
		classifier = Segmenter.fromJson(ops, GsonUtils.read(path));
		isTrained = true;
		listeners.forEach(l -> l.notify(this));
	}
}
