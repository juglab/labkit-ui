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

package sc.fiji.labkit.ui.utils;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Matthias Arzt
 */
public class DimensionUtils {

	public static <T> List<RandomAccessibleInterval<T>> slices(
		RandomAccessibleInterval<T> output)
	{
		int axis = output.numDimensions() - 1;
		return LongStream.range(output.min(axis), output.max(axis) + 1).mapToObj(
			pos -> Views.hyperSlice(output, axis, pos)).collect(Collectors.toList());
	}

	// TODO: move to Intervals?
	public static Interval appendDimensionToInterval(Interval in, long min,
		long max)
	{
		int n = in.numDimensions();
		long[] mins = new long[n + 1];
		long[] maxs = new long[n + 1];
		for (int i = 0; i < n; i++) {
			mins[i] = in.min(i);
			maxs[i] = in.max(i);
		}
		mins[n] = min;
		maxs[n] = max;
		return new FinalInterval(mins, maxs);
	}

	public static Interval removeLastDimension(Interval in) {
		long[] min = removeLast(Intervals.minAsLongArray(in));
		long[] max = removeLast(Intervals.maxAsLongArray(in));
		return new FinalInterval(min, max);
	}

	private static long[] removeLast(long[] longs) {
		return Arrays.copyOf(longs, longs.length - 1);
	}

	public static Interval intervalRemoveDimension(Interval interval, int d) {
		long[] min = removeElement(Intervals.minAsLongArray(interval), d);
		long[] max = removeElement(Intervals.maxAsLongArray(interval), d);
		return new FinalInterval(min, max);
	}

	private static long[] removeElement(long[] values, int d) {
		long[] result = new long[values.length - 1];
		System.arraycopy(values, 0, result, 0, d);
		System.arraycopy(values, d + 1, result, d, values.length - d - 1);
		return result;
	}
}
