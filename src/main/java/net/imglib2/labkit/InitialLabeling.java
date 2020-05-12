
package net.imglib2.labkit;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class InitialLabeling {

	static void initializeLabeling(InputImage inputImage,
		DefaultSegmentationModel segmentationModel)
	{
		final Context context = segmentationModel.context();
		Preferences preferences = new Preferences(context);
		List<String> defaultLabels = preferences.getDefaultLabels();
		Labeling initialLabeling = initLabeling(inputImage, context, defaultLabels);
		segmentationModel.imageLabelingModel().labeling().set(initialLabeling);
	}

	static Labeling initLabeling(InputImage inputImage, Context context,
		List<String> defaultLabels)
	{
		String filename = inputImage.getDefaultLabelingFilename();
		if (new File(filename).exists()) try {
			return openLabeling(inputImage, context, filename);
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return defaultLabeling(inputImage, defaultLabels);
	}

	private static Labeling defaultLabeling(InputImage inputImage,
		List<String> defaultLabels)
	{
		ImgPlus<?> image = inputImage.imageForSegmentation();
		Interval bigInterval = new FinalInterval(ImgPlusViewsOld.hyperSlice(image, Axes.CHANNEL, 0));
		Interval smallInterval = askShrinkInterval(bigInterval);
		int scaling = getIntegerScale(smallInterval, bigInterval).getAsInt();
		Labeling labeling = Labeling.createEmpty(defaultLabels, smallInterval);
		labeling.setAxes(scaledAxes(scaling, filterChannelAxis(ImgPlusViewsOld.getCalibratedAxes(
			image))));
		return labeling;
	}

	private static List<CalibratedAxis> filterChannelAxis(List<CalibratedAxis> calibratedAxes) {
		return calibratedAxes.stream().filter(axis -> axis.type() != Axes.CHANNEL).collect(Collectors
			.toList());
	}

	private static Labeling openLabeling(InputImage inputImage, Context context,
		String filename) throws IOException
	{
		Labeling open = new LabelingSerializer(context).open(filename);
		fixAxes(open, inputImage);
		return open;
	}

	private static void fixAxes(Labeling labeling, InputImage image) {
		ImgPlus<? extends NumericType<?>> imgPlus = image.imageForSegmentation();
		if (!imgPlus.getName().endsWith(".czi")) return;
		List<CalibratedAxis> labelingAxes = labeling.axes();
		List<CalibratedAxis> imageAxes = filterChannelAxis(ImgPlusViewsOld.getCalibratedAxes(imgPlus));
		Interval imageInterval = imgPlus;
		Interval labelingInterval = labeling.interval();
		OptionalDouble optionalDouble = getScale(labelingInterval, imageInterval);
		if (!optionalDouble.isPresent()) {
			String message = "\"" + image.getDefaultLabelingFilename() + "\"\n" +
				"The labeling does not match the image sizes.\n" +
				"Labkit might give wrong results.\n" +
				"It's recommended to delete the labeling and start from scratch again.";
			JOptionPane.showMessageDialog(null, message, "WARNING",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		double scale = optionalDouble.getAsDouble();
		boolean calibrationCorrect = IntStream.range(0, labelingAxes.size())
			.allMatch(i -> getLinearScale(imageAxes.get(i)) * scale == getLinearScale(
				labelingAxes.get(i)));
		if (calibrationCorrect) return;
		int result = JOptionPane.showConfirmDialog(null,
			"Labeling contains wrong calibration information.\nFix it?",
			"Open Labeling", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) labeling.setAxes(scaledAxes(scale,
			imageAxes));
	}

	private static OptionalDouble getScale(Interval labelingInterval,
		Interval imageInterval)
	{
		OptionalInt scale = getIntegerScale(labelingInterval, imageInterval);
		if (scale.isPresent()) return OptionalDouble.of(scale.getAsInt());
		OptionalInt reverseScale = getIntegerScale(imageInterval, labelingInterval);
		if (reverseScale.isPresent()) return OptionalDouble.of(1.0 / reverseScale
			.getAsInt());
		return OptionalDouble.empty();
	}

	private static OptionalInt getIntegerScale(Interval small, Interval big) {
		int scale = (int) (big.dimension(0) / small.dimension(0));
		if (scale <= 0) return OptionalInt.empty();
		boolean isIntegerScale = IntStream.range(0, small.numDimensions()).allMatch(
			i -> big.dimension(i) / scale == small.dimension(i));
		return isIntegerScale ? OptionalInt.of(scale) : OptionalInt.empty();
	}

	private static List<CalibratedAxis> scaledAxes(double factor,
		List<CalibratedAxis> imageAxes)
	{
		return imageAxes.stream().map(axis -> scaledAxes(factor,
			(DefaultLinearAxis) axis)).collect(Collectors.toList());
	}

	private static CalibratedAxis scaledAxes(double factor,
		DefaultLinearAxis input)
	{
		return new DefaultLinearAxis(input.type(), input.unit(), input.scale() *
			factor, input.origin());
	}

	private static double getLinearScale(CalibratedAxis downScaled) {
		return ((DefaultLinearAxis) downScaled).scale();
	}

	static private Interval askShrinkInterval(Interval interval) {
		if (interval.numDimensions() != 2) return interval;
		if (!consideredBig(interval)) return interval;
		interval = new FinalInterval(interval);
		List<Interval> suggestions = new ArrayList<>();
		suggestions.add(interval);
		while (consideredBig(interval)) {
			interval = shrink(interval);
			suggestions.add(interval);
		}
		List<String> texts = suggestions.stream().map(i -> i.dimension(0) + "x" + i
			.dimension(1)).collect(Collectors.toList());
		Object selected = JOptionPane.showInputDialog(null,
			"Select resultion of the labeling", "Labekit", JOptionPane.PLAIN_MESSAGE,
			null, texts.toArray(), texts.get(texts.size() - 1));
		int index = texts.indexOf(selected);
		return (index >= 0) ? suggestions.get(index) : interval;
	}

	static private Interval shrink(Interval interval) {
		long[] dimensions = Intervals.dimensionsAsLongArray(interval);
		long[] newDimensions = LongStream.of(dimensions).map(x -> x / 2).toArray();
		return new FinalInterval(newDimensions);
	}

	static private boolean consideredBig(Interval interval) {
		return Intervals.numElements(interval) > 10000000;
	}
}
