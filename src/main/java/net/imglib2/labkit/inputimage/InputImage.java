
package net.imglib2.labkit.inputimage;

import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.NumericType;

import java.util.List;

public interface InputImage {

	RandomAccessibleInterval<? extends NumericType<?>> imageForSegmentation();

	default BdvShowable showable() {
		return BdvShowable.wrap(imageForSegmentation());
	}

	default Interval interval() {
		return new FinalInterval(imageForSegmentation());
	}

	ChannelSetting getChannelSetting();

	int getSpatialDimensions();

	String getDefaultLabelingFilename();

	String getName();

	List<CalibratedAxis> axes();

	boolean isTimeSeries();

	default boolean isMultiChannel() {
		return false;
	}
}
