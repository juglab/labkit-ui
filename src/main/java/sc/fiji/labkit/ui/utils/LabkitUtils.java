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

import bdv.export.ProgressWriter;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.parallel.Parallelization;
import net.imglib2.parallel.TaskExecutor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * @author Matthias Arzt
 */
public class LabkitUtils {

	public static long[] extend(long[] array, long value) {
		int length = array.length;
		long[] result = new long[length + 1];
		System.arraycopy(array, 0, result, 0, length);
		result[length] = value;
		return result;
	}

	public static int[] extend(int[] array, int value) {
		int length = array.length;
		int[] result = new int[length + 1];
		System.arraycopy(array, 0, result, 0, length);
		result[length] = value;
		return result;
	}

	public static RandomAccessibleInterval<FloatType> toFloat(
		RandomAccessibleInterval<? extends RealType<?>> rawData)
	{
		return Converters.convert(rawData, (in, out) -> out.set(in.getRealFloat()),
			new FloatType());
	}

	public static void copy(RandomAccessible<? extends IntegerType<?>> source,
		RandomAccessibleInterval<? extends IntegerType<?>> dest)
	{
		Views.interval(Views.pair(source, dest), dest).forEach(p -> p.getB()
			.setInteger(p.getA().getInteger()));
	}

	public static Img<UnsignedByteType> copyUnsignedBytes(
		RandomAccessibleInterval<? extends IntegerType<?>> source)
	{
		final long[] dimensions = Intervals.dimensionsAsLongArray(source);
		Img<UnsignedByteType> dest = ArrayImgs.unsignedBytes(dimensions);
		copy(source, dest);
		return dest;
	}

	public static <R extends NumericType<?>> Pair<Double, Double> estimateMinMax(
		RandomAccessibleInterval<R> rawData)
	{
		R firstElement = rawData.randomAccess().get();
		if (firstElement instanceof UnsignedByteType) return new ValuePair<>(0.,
			255.);
		if (firstElement instanceof ARGBType) return new ValuePair<>(0., 255.);
		if (firstElement instanceof UnsignedShortType)
			return tryEstimateMinMax(
				Cast.unchecked(rawData), 0., 256. * 256. - 1);
		if (firstElement instanceof RealType)
			return tryEstimateMinMax(Cast.unchecked(rawData), 0., 1.);
		return new ValuePair<>(0., 255.);
	}

	private static Pair<Double, Double> tryEstimateMinMax(
		RandomAccessibleInterval<? extends RealType<?>> rawData, double defaultMin,
		double defaultMax)
	{
		long size = Intervals.numElements(rawData);
		if (size > 1e8) return new ValuePair<>(defaultMin, defaultMax);
		return calculateMinMax(rawData);
	}

	private static Pair<Double, Double> calculateMinMax(
		RandomAccessibleInterval<? extends RealType<?>> rawData)
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (RealType<?> value : Views.iterable(rawData)) {
			double d = value.getRealDouble();
			min = Math.min(min, d);
			max = Math.max(max, d);
		}
		return new ValuePair<>(min, max);
	}

}
