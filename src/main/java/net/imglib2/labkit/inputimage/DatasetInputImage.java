package net.imglib2.labkit.inputimage;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

import java.util.ArrayList;
import java.util.List;

public class DatasetInputImage extends AbstractInputImage {

	private final ImgPlus<?> image;
	private boolean isTimeSeries;

	public DatasetInputImage(ImgPlus<?> image) {
		this.image = image;
		this.isTimeSeries = image.dimensionIndex(Axes.TIME) >= 0;
	}

	public DatasetInputImage(Dataset image) {
		this(image.getImgPlus());
	}

	@Override
	public RandomAccessibleInterval<? extends NumericType<?>> displayImage() {
		if (image.randomAccess().get() instanceof ARGBType)
			return (RandomAccessibleInterval<? extends NumericType<?>>) image;
		int index = image.dimensionIndex(Axes.CHANNEL);
		if(index >= 0 && image.dimension(index) == 3)
			return fuseColors((ImgPlus<RealType<?>>) image);
		else
			return (RandomAccessibleInterval<? extends NumericType<?>>) image;
	}

	private RandomAccessibleInterval<ARGBType> fuseColors(ImgPlus<RealType<?>> dataset) {
		int colorAxis = dataset.dimensionIndex(Axes.CHANNEL);
		if(colorAxis < 0)
			throw new UnsupportedOperationException();
		int lastAxis = dataset.numDimensions() - 1;
		RandomAccessibleInterval<RealType<?>> sorted = colorAxis == lastAxis ? dataset :
				Views.permute(dataset, colorAxis, lastAxis);
		RandomAccessibleInterval<? extends GenericComposite<RealType<?>>> colors = Views.collapse(sorted);
		return Converters.convert(colors, (in, out) -> out.set(compositeToInt(in)), new ARGBType());
	}

	private int compositeToInt(GenericComposite<RealType<?>> in) {
		int r = (int) in.get(0).getRealFloat();
		int g = (int) in.get(1).getRealFloat();
		int b = (int) in.get(2).getRealFloat();
		return ARGBType.rgba(r, g, b, 255);
	}

	@Override
	public ChannelSetting getChannelSetting() {
		int channelIndex = image.dimensionIndex(Axes.CHANNEL);
		if(channelIndex < 0)
			return ChannelSetting.SINGLE;
		long numberOfChannels = image.dimension(channelIndex);
		if(numberOfChannels == 3)
			return ChannelSetting.RGB;
		throw new IllegalArgumentException("Image must be single channel or rgb.");
	}

	@Override
	public int getSpatialDimensions() {
		return image.numDimensions() - (isTimeSeries() ? 1 : 0) - (hasColorChannel() ? 1 : 0);
	}

	@Override
	public String getFilename() {
		return image.getSource();
	}

	@Override
	public String getName() {
		return image.getName();
	}

	@Override
	public List<CalibratedAxis> axes() {
		List<CalibratedAxis> axes = new ArrayList<>();
		for (int i = 0; i < image.numDimensions(); i++) {
			CalibratedAxis axis = image.axis(i);
			if(axis.type() != Axes.CHANNEL)
				axes.add(axis);
		}
		return axes;
	}

	@Override
	public boolean isTimeSeries() {
		return isTimeSeries;
	}

	private boolean hasColorChannel() {
		return image.dimensionIndex(Axes.CHANNEL) >= 0;
	}

	public void setTimeSeries(boolean value) {
		this.isTimeSeries = value;
	}
}
