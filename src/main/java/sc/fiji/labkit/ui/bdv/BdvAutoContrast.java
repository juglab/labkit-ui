
package sc.fiji.labkit.ui.bdv;

import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.Random;

/**
 * Calling {@link BdvAutoContrast#autoContrast(BdvSource)} will set the contrast
 * of the given {@link BdvSource} to reasonable values.
 */
public class BdvAutoContrast {

	/**
	 * Set display min/max of {@code bdvSource} to min/max estimated by random
	 * sampling, computed at coarsest resolution for the current timepoint.
	 */
	public static void autoContrast(BdvSource bdvSource) {
		ValuePair<Double, Double> minMax = getMinMax(bdvSource);
		bdvSource.setDisplayRangeBounds(minMax.getA(), minMax.getB());
		bdvSource.setDisplayRange(minMax.getA(), minMax.getB());
	}

	private static ValuePair<Double, Double> getMinMax(BdvSource bdvSource) {
		ViewerPanel viewer = bdvSource.getBdvHandle().getViewerPanel();
		Source<?> spimSource = ((BdvStackSource<?>) bdvSource).getSources().get(0).getSpimSource();
		int level = spimSource.getNumMipmapLevels() - 1;
		RandomAccessibleInterval<?> source = spimSource.getSource(viewer.state()
			.getCurrentTimepoint(), level);
		if (Util.getTypeFromInterval(source) instanceof RealType)
			return getMinMaxForRealType(Cast.unchecked(source));
		return new ValuePair<>(0.0, 255.0);
	}

	private static ValuePair<Double, Double> getMinMaxForRealType(
		RandomAccessibleInterval<? extends RealType<?>> source)
	{
		Cursor<? extends RealType<?>> cursor = Views.iterable(source).cursor();
		if (!cursor.hasNext()) return new ValuePair<>(0.0, 255.0);
		long stepSize = Intervals.numElements(source) / 10000 + 1;
		int randomLimit = (int) Math.min(Integer.MAX_VALUE, stepSize);
		Random random = new Random(42);
		double min = cursor.next().getRealDouble();
		double max = min;
		while (cursor.hasNext()) {
			double value = cursor.get().getRealDouble();
			cursor.jumpFwd(stepSize + random.nextInt(randomLimit));
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		return new ValuePair<>(min, max);
	}
}
