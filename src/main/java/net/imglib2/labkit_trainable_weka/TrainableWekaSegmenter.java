
package net.imglib2.labkit_trainable_weka;

import ij.ImagePlus;
import ij.gui.NewImage;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.IterableRegion;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import trainableSegmentation.FeatureStack;
import trainableSegmentation.WekaSegmentation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TrainableWekaSegmenter implements Segmenter {

	WekaSegmentation wekaSegmentation;
	int border = 40;

	public TrainableWekaSegmenter(Context context, InputImage inputImage) {}

	@Override
	public void editSettings(JFrame dialogParent) {}

	@Override
	public void train(
		List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> trainingData)
	{
		wekaSegmentation = new WekaSegmentation();
		boolean[] features = new boolean[FeatureStack.availableFeatures.length];
		features[FeatureStack.GAUSSIAN] = true;
		wekaSegmentation.setEnabledFeatures(features);
		List<String> classNames = classNames(trainingData);
		for (Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> pair : trainingData)
			addTrainingData(pair.getA(), pair.getB());
		wekaSegmentation.setClassLabels(classNames.toArray(new String[0]));
		wekaSegmentation.trainClassifier();
	}

	private List<String> classNames(
		List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> trainingData)
	{
		Set<String> set = trainingData.stream().flatMap(
			imageAndLabels -> imageAndLabels.getB().getLabels().stream().map(
				Label::name)).collect(Collectors.toSet());
		return new ArrayList<>(set);
	}

	private void addTrainingData(RandomAccessibleInterval<?> image,
		Labeling labeling)
	{
		ImagePlus imagePlus = imageToImagePlus(image);
		ImagePlus labels = labelToImagePlus(labeling);
		wekaSegmentation.addLabeledData(imagePlus, labels);
	}

	private ImagePlus imageToImagePlus(RandomAccessibleInterval image) {
		return ImageJFunctions.wrap(image, "...");
	}

	private ImagePlus labelToImagePlus(Labeling labeling) {
		List<Label> labels = labeling.getLabels();
		ImagePlus result = NewImage.createByteImage("labeling", (int) labeling
			.dimension(0), (int) labeling.dimension(1), 1, 0);
		Img<UnsignedByteType> r = ImagePlusAdapter.wrapByte(result);
		r.forEach(pixel -> pixel.setByte((byte) -1));
		Map<Label, IterableRegion<BitType>> regions = labeling.iterableRegions();
		for (int i = 0; i < labels.size(); i++) {
			final Label o = labels.get(i);
			final IterableRegion<BitType> region = regions.get(o);
			Cursor<Void> cursor = region.cursor();
			RandomAccess<UnsignedByteType> ra = r.randomAccess();
			while (cursor.hasNext()) {
				cursor.fwd();
				ra.setPosition(cursor);
				ra.get().set(i);
			}
		}
		return result;
	}

	@Override
	public void segment(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		if (!isTrained()) return;
		ImagePlus crop = imageToImagePlus(Views.interval(Views.extendBorder(image),
			Intervals.expand(outputSegmentation, border)));
		ImagePlus segmentation = wekaSegmentation.applyClassifier(crop);
		copyUnsignedByteTypeFromTo(segmentation, outputSegmentation, border);
	}

	private void copyUnsignedByteTypeFromTo(ImagePlus segmentation,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation,
		int border)
	{
		final Img<UnsignedByteType> img = ImageJFunctions.wrapByte(segmentation);
		IntervalView<UnsignedByteType> crop = Views.interval(img, Intervals.expand(
			img, -border));
		LoopBuilder.setImages(crop, outputSegmentation).forEachPixel((i, o) -> o
			.setInteger(i.getInteger()));
	}

	@Override
	public void predict(RandomAccessibleInterval<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		if (!isTrained()) return;
		Interval outputInterval = RevampUtils.removeLastDimension(
			outputProbabilityMap);
		ImagePlus crop = imageToImagePlus(Views.interval(Views.extendBorder(image),
			Intervals.expand(outputInterval, border)));
		ImagePlus probabilityMaps = wekaSegmentation.applyClassifier(crop, 1, true);
		Img<FloatType> pmImg = ImageJFunctions.wrapFloat(probabilityMaps);
		long[] b = { -border, -border, 0 };
		RandomAccessibleInterval<FloatType> pmImgCropped = Views.interval(pmImg,
			Intervals.expand(pmImg, b));
		LoopBuilder.setImages(pmImgCropped, outputProbabilityMap).forEachPixel((i,
			o) -> o.setReal(i.getRealFloat()));
	}

	@Override
	public boolean isTrained() {
		return wekaSegmentation != null;
	}

	@Override
	public void saveModel(String path) {
		wekaSegmentation.saveClassifier(path);
	}

	@Override
	public void openModel(String path) {
		wekaSegmentation.loadClassifier(path);
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList(wekaSegmentation.getClassLabels());
	}
}
