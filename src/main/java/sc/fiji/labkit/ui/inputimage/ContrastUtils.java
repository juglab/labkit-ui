
package sc.fiji.labkit.ui.inputimage;

import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

/**
 * Helper for {@link DatasetInputImage} that picks the correct contrast when
 * showing an ImgPlus.
 */
class ContrastUtils {

	public static double getMin(ImgPlus<?> image) {
		double min = 0;
		for (int i = 0;; i++) {
			double value = image.getChannelMinimum(i);
			if (Double.isNaN(value)) break;
			min = Math.min(value, min);
		}
		return min;
	}

	public static double getMax(ImgPlus<? extends NumericType<?>> image) {
		double max = 0;
		for (int i = 0;; i++) {
			double value = image.getChannelMaximum(i);
			if (Double.isNaN(value)) break;
			max = Math.max(value, max);
		}
		return max;
	}

	public static BdvShowable showableAddSetDisplayRange(BdvShowable wrap,
		double min, double max)
	{
		return new BdvShowable() {

			@Override
			public Interval interval() {
				return wrap.interval();
			}

			@Override
			public AffineTransform3D transformation() {
				return wrap.transformation();
			}

			@Override
			public BdvStackSource<?> show(String title, BdvOptions options) {
				final BdvStackSource<?> result = wrap.show(title, options);
				result.setDisplayRange(min, max);
				return result;
			}
		};
	}
}
