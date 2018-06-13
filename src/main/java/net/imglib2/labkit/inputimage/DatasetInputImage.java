
package net.imglib2.labkit.inputimage;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: the management of color channels, is spread over the whole class, improve this.
public class DatasetInputImage extends AbstractInputImage {

	private final ImgPlus<? extends NumericType<?>> image;
	private final BdvShowable showable;
	private boolean isTimeSeries;
	private String labelingName;

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image,
		BdvShowable showable)
	{
		this.showable = showable;
		this.image = tryFuseColor(image);
		this.isTimeSeries = image.dimensionIndex(Axes.TIME) >= 0;
		this.labelingName = image.getSource() + ".labeling";
	}

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image) {
		this(image, BdvShowable.wrap(tryFuseColor(image)));
	}

	public DatasetInputImage(Dataset image) {
		this(image.getImgPlus());
	}

	private static ImgPlus<? extends NumericType<?>> tryFuseColor(
		ImgPlus<? extends NumericType<?>> image)
	{
		if (!(image.randomAccess().get() instanceof RealType)) return image;
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
		return image.randomAccess().get() instanceof ARGBType ? ChannelSetting.RGB
			: ChannelSetting.SINGLE;
	}

	@Override
	public int getSpatialDimensions() {
		return image.numDimensions() - (isTimeSeries() ? 1 : 0);
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
		return IntStream.range(0, image.numDimensions()).mapToObj(image::axis)
			.collect(Collectors.toList());
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	public void setTimeSeries(boolean value) {
		this.isTimeSeries = value;
	}
}
