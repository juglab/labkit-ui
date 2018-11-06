
package net.imglib2.labkit.bdv;

import bdv.util.BdvSource;
import bdv.viewer.ViewerPanel;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.Random;

public class BdvAutoContrast {

	public static void autoContrast(BdvSource bdvSource) {
		ViewerPanel viewer = bdvSource.getBdvHandle().getViewerPanel();
		bdvSource.setCurrent();
		RandomAccessibleInterval<?> source = viewer.getState().getSources().get(
			viewer.getState().getCurrentSource()).getSpimSource().getSource(viewer
				.getState().getCurrentTimepoint(), 0);
		ValuePair<Double, Double> minMax = getMinMax(
			(RandomAccessibleInterval<? extends RealType<?>>) source);
		bdvSource.setDisplayRangeBounds(minMax.getA(), minMax.getB());
		bdvSource.setDisplayRange(minMax.getA(), minMax.getB());
	}

	private static ValuePair<Double, Double> getMinMax(
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
