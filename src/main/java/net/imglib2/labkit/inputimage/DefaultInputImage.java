package net.imglib2.labkit.inputimage;

import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultInputImage extends AbstractInputImage {

	private final RandomAccessibleInterval<? extends NumericType<?>> image;

	private final NumericType type;

	private boolean isTimeSeries = false;

	private String filename;

	public DefaultInputImage(RandomAccessibleInterval<? extends NumericType<?>> image) {
		this.image = image;
		this.type = Util.getTypeFromInterval(image);
	}

	@Override
	public RandomAccessibleInterval<? extends NumericType<?>> displayImage() {
		return image;
	}

	@Override
	public ChannelSetting getChannelSetting() {
		return type instanceof ARGBType ? ChannelSetting.RGB : ChannelSetting.SINGLE;
	}

	@Override
	public int getSpatialDimensions() {
		return image.numDimensions() - (isTimeSeries() ? 1 : 0);
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public List<CalibratedAxis> axes() {
		return IntStream.range(0, image.numDimensions())
				.mapToObj(ignore -> new DefaultLinearAxis())
				.collect(Collectors.toList());
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	public void setTimeSeries(boolean value) {
		isTimeSeries = value;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
