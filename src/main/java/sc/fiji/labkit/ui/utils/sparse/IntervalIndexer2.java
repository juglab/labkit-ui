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

package sc.fiji.labkit.ui.utils.sparse;

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
		return LongStream.of(dimensions).reduce(1, (a, b) -> a * b);
	}

	public long positionToIndex(Localizable localizable) {
		long sum = 0;
		for (int d = 0; d < dimensions.length; ++d)
			sum += stepSize[d] * (localizable.getLongPosition(d) - min[d]);
		return sum;
	}

	public void indexToPosition(long index, Positionable positionable) {
		for (int d = 0; d < dimensions.length; ++d)
			positionable.setPosition(indexToPosition(index, d), d);
	}

	public long indexToPosition(long index, int d) {
		return index / stepSize[d] % dimensions[d] + min[d];
	}
}
