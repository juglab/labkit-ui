
package sc.fiji.labkit.ui.utils;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

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

	public static long[] extend(long[] in, long elem) {
		long result[] = new long[in.length + 1];
		System.arraycopy(in, 0, result, 0, in.length);
		result[in.length] = elem;
		return result;
	}

	public static int[] extend(int[] in, int elem) {
		int result[] = new int[in.length + 1];
		System.arraycopy(in, 0, result, 0, in.length);
		result[in.length] = elem;
		return result;
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
