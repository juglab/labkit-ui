
package net.imglib2.labkit.inputimage;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Cast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: the management of color channels, is spread over the whole class, improve this.
public class DatasetInputImage extends AbstractInputImage {

	private final ImgPlus<? extends NumericType<?>> image;
	private final BdvShowable showable;
	private String defaultLabelingFilename;

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image,
		BdvShowable showable)
	{
		this.showable = showable;
		this.image = prepareImage(image);
		this.defaultLabelingFilename = image.getSource() + ".labeling";
	}

	public DatasetInputImage(Img<?> image) {
		this(ImgPlus.wrap(image));
	}

	private static ImgPlus<? extends NumericType<?>> prepareImage(
		ImgPlus<? extends NumericType<?>> image)
	{
		List<AxisType> order = Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL,
			Axes.TIME);
		return ImgPlusViewsOld.sortAxes(tryFuseColor(labelAxes(image)), order);
	}

	private static ImgPlus<? extends NumericType<?>> labelAxes(
		ImgPlus<? extends NumericType<?>> image)
	{
		if (image.firstElement() instanceof ARGBType) return ImgPlusViewsOld
			.fixAxes(image, Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.TIME));
		if (image.numDimensions() == 4) return ImgPlusViewsOld.fixAxes(image, Arrays
			.asList(Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL));
		return ImgPlusViewsOld.fixAxes(image, Arrays.asList(Axes.X, Axes.Y, Axes.Z,
			Axes.CHANNEL, Axes.TIME));
	}

	public DatasetInputImage(ImgPlus<?> image) {
		this(Cast.unchecked(image), initializeShowable(Cast.unchecked(image)));
	}

	public static BdvShowable initializeShowable(
		ImgPlus<? extends NumericType<?>> image)
	{
		final BdvShowable showable = BdvShowable.wrap(prepareImage(image));
		final double min = ContrastUtils.getMin(image);
		final double max = ContrastUtils.getMax(image);
		return (min < max) ? ContrastUtils.showableAddSetDisplayRange(showable, min,
			max) : showable;
	}

	public DatasetInputImage(Dataset image) {
		this(image.getImgPlus());
	}

	private static ImgPlus<? extends NumericType<?>> tryFuseColor(
		ImgPlus<? extends NumericType<?>> image)
	{
		if (!(image.randomAccess().get() instanceof UnsignedByteType)) return image;
		@SuppressWarnings("unchecked")
		ImgPlus<RealType<?>> image1 = (ImgPlus<RealType<?>>) image;
		if (ImgPlusViews.canFuseColor(image1)) return ImgPlusViews.fuseColor(
			image1);
		return image1;
	}

	@Override
	public BdvShowable showable() {
		return showable;
	}

	@Override
	public ImgPlus<? extends NumericType<?>> imageForSegmentation() {
		return image;
	}

	public void setDefaultLabelingFilename(String defaultLabelingFilename) {
		this.defaultLabelingFilename = defaultLabelingFilename;
	}

	@Override
	public String getDefaultLabelingFilename() {
		return defaultLabelingFilename;
	}

}
