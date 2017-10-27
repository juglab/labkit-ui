package net.imglib2.atlas;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

public class InputImage {

	private final Dataset dataset;

	public InputImage(Dataset dataset) {
		this.dataset = dataset;
	}

	public RandomAccessibleInterval<? extends NumericType<?>> displayImage() {
		if(dataset.dimension(Axes.CHANNEL) != 3)
			return dataset;
		else
			return fuseColors(dataset);
	}

	private RandomAccessibleInterval<ARGBType> fuseColors(Dataset dataset) {
		if(dataset.dimension(Axes.CHANNEL) != 3)
			throw new UnsupportedOperationException();
		int colorAxis = dataset.dimensionIndex(Axes.CHANNEL);
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

	public ChannelSetting getChannelSetting() {
		int channelIndex = dataset.dimensionIndex(Axes.CHANNEL);
		if(channelIndex < 0)
			return ChannelSetting.SINGLE;
		long numberOfChannels = dataset.dimension(channelIndex);
		if(numberOfChannels == 1)
			return ChannelSetting.SINGLE;
		if(numberOfChannels == 3)
			return ChannelSetting.RGB;
		throw new IllegalArgumentException("Image must be single channel or rgb.");
	}

	public int getSpatialDimensions() {
		return dataset.axis(Axes.Z).isPresent() || dataset.axis(Axes.TIME).isPresent() ? 3 : 2;
	}
}
