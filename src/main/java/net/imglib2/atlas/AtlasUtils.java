package net.imglib2.atlas;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/*
 * @author Matthias Arzt
 */
public class AtlasUtils {
	static long[] extend(long[] array, long value) {
		int length = array.length;
		long[] result = new long[length + 1];
		System.arraycopy(array, 0, result, 0, length);
		result[length] = value;
		return result;
	}

	static int[] extend(int[] array, int value) {
		int length = array.length;
		int[] result = new int[length + 1];
		System.arraycopy(array, 0, result, 0, length);
		result[length] = value;
		return result;
	}

	public static <R,T> R uncheckedCast(T value) {
		@SuppressWarnings("unchecked") R r = (R) value;
		return r;
	}

	static RandomAccessibleInterval<FloatType> toFloat(RandomAccessibleInterval<? extends RealType<?>> rawData) {
		return Converters.convert(rawData, (in, out) -> out.set(in.getRealFloat()), new FloatType());
	}
}
