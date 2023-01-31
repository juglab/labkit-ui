/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
