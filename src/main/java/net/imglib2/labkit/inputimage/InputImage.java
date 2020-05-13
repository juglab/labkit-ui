
package net.imglib2.labkit.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.type.numeric.NumericType;

import java.util.List;

public interface InputImage {

	ImgPlus<? extends NumericType<?>> imageForSegmentation();

	default BdvShowable showable() {
		return BdvShowable.wrap(imageForSegmentation());
	}

	String getDefaultLabelingFilename();
}
