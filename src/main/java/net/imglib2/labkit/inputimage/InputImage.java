package net.imglib2.labkit.inputimage;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.NumericType;

import java.util.List;

public interface InputImage {
	RandomAccessibleInterval<? extends NumericType<?>> displayImage();

	ChannelSetting getChannelSetting();

	int getSpatialDimensions();

	String getFilename();

	String getName();

	List<CalibratedAxis> axes();

	boolean isTimeSeries();
}
