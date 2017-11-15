package net.imglib2.atlas.inputimage;

import net.imagej.Dataset;
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

public class DatasetInputImage implements InputImage {

	private final Dataset dataset;
	private boolean isTimeSeries;

	public DatasetInputImage(Dataset dataset) {
		this.dataset = dataset;
		this.isTimeSeries = dataset.axis(Axes.TIME).isPresent();
	}

	@Override
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

	@Override
	public ChannelSetting getChannelSetting() {
		int channelIndex = dataset.dimensionIndex(Axes.CHANNEL);
		if(channelIndex < 0)
			return ChannelSetting.SINGLE;
		long numberOfChannels = dataset.dimension(channelIndex);
		if(numberOfChannels == 3)
			return ChannelSetting.RGB;
		throw new IllegalArgumentException("Image must be single channel or rgb.");
	}

	@Override
	public int getSpatialDimensions() {
		return dataset.numDimensions() - (isTimeSeries() ? 1 : 0) - (hasColorChannel() ? 1 : 0);
	}

	@Override
	public String getFilename() {
		return dataset.getSource();
	}

	@Override
	public String getName() {
		return dataset.getName();
	}

	@Override
	public List<CalibratedAxis> axes() {
		List<CalibratedAxis> axes = new ArrayList<>();
		for (int i = 0; i < dataset.numDimensions(); i++) {
			CalibratedAxis axis = dataset.axis(i);
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
		return dataset.axis(Axes.CHANNEL).isPresent();
	}

	public void setTimeSeries(boolean value) {
		this.isTimeSeries = value;
	}
}
