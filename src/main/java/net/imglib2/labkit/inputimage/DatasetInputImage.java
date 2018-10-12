
package net.imglib2.labkit.inputimage;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: the management of color channels, is spread over the whole class, improve this.
public class DatasetInputImage extends AbstractInputImage {

	private final ImgPlus<? extends NumericType<?>> image;
	private final BdvShowable showable;
	private boolean isTimeSeries;
	private String labelingName;
	private boolean isMultiChannel = false;

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image,
		BdvShowable showable)
	{
		this.showable = showable;
		this.image = prepareImage(image);
		this.isMultiChannel = this.image.dimensionIndex(Axes.CHANNEL) >= 0;
		this.isTimeSeries = this.image.dimensionIndex(Axes.TIME) >= 0;
		this.labelingName = image.getSource() + ".labeling";
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

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image) {
		this(image, initializeShowable(image));
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

	@Override
	public Interval interval() {
		int colorAxis = image.numDimensions() - 1 - (isTimeSeries() ? 1 : 0);
		return isMultiChannel ? RevampUtils.intervalRemoveDimension(image,
			colorAxis) : image;
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
	public RandomAccessibleInterval<? extends NumericType<?>>
		imageForSegmentation()
	{
		return image;
	}

	@Override
	public ChannelSetting getChannelSetting() {
		if (isMultiChannel()) return ChannelSetting.multiple((int) image.dimension(
			image.numDimensions() - 1));
		return image.randomAccess().get() instanceof ARGBType ? ChannelSetting.RGB
			: ChannelSetting.SINGLE;
	}

	@Override
	public int getSpatialDimensions() {
		return image.numDimensions() - (isTimeSeries() ? 1 : 0) - (isMultiChannel()
			? 1 : 0);
	}

	public void setDefaultLabelingFilename(String filename) {
		this.labelingName = filename;
	}

	@Override
	public String getDefaultLabelingFilename() {
		return labelingName;
	}

	@Override
	public String getName() {
		return image.getName();
	}

	@Override
	public List<CalibratedAxis> axes() {
		List<CalibratedAxis> allAxes = IntStream.range(0, image.numDimensions())
			.mapToObj(image::axis).collect(Collectors.toList());
		if (isMultiChannel()) {
			int channelAxis = getSpatialDimensions();
			return removeElement(allAxes, channelAxis);
		}
		return allAxes;
	}

	private <T> List<T> removeElement(List<T> list, int index) {
		List<T> result = new ArrayList<>(list);
		result.remove(index);
		return result;
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	@Override
	public boolean isMultiChannel() {
		return isMultiChannel;
	}
}
