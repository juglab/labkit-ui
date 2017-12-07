package net.imglib2.sparse;

import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.util.Intervals;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author Matthias Arzt
 */
public class IntervalIndexer2 {

	private final long[] min;
	private final long[] dimensions;
	private final long[] stepSize;

	public IntervalIndexer2(final Interval interval) {
		min = Intervals.minAsLongArray(interval);
		dimensions = Intervals.dimensionsAsLongArray(interval);
		stepSize = initStepStepSize();
	}

	private long[] initStepStepSize() {
		long[] stepSize = new long[dimensions.length];
		stepSize[0] = 1;
		for (int i = 1; i < dimensions.length; i++)
			stepSize[i] = stepSize[i - 1] * dimensions[i - 1];
		return stepSize;
	}

	public long size() {
		return LongStream.of(dimensions).reduce(1, (a,b) -> a * b);
	}

	public long positionToIndex(Localizable localizable) {
		long sum = 0;
		for (int d = 0; d < dimensions.length; ++d )
			sum += stepSize[d] * (localizable.getLongPosition( d ) - min[d]);
		return sum;
	}

	public void indexToPosition(long index, Positionable positionable) {
		for (int d = 0; d < dimensions.length; ++d )
			positionable.setPosition( indexToPosition(index, d), d );
	}

	public long indexToPosition(long index, int d) {
		return index / stepSize[d] % dimensions[d] + min[d];
	}
}
