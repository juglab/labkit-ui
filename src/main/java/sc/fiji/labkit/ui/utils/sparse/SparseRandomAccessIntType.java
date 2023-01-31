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

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.AbstractWrappedInterval;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.integer.IntType;

/**
 * @author Matthias Arzt
 */
public class SparseRandomAccessIntType extends AbstractWrappedInterval<Interval>
	implements RandomAccessibleInterval<IntType>
{

	private final ReadRetryWriteLock lock = new ReadRetryWriteLock();
	private final IntervalIndexer2 indexer;
	private final TLongIntHashMap values;
	private final int noEntryValue;

	public SparseRandomAccessIntType(Interval source) {
		this(source, 0);
	}

	public SparseRandomAccessIntType(Interval source, int noEntryValue) {
		super(source);
		this.indexer = new IntervalIndexer2(source);
		this.values = new TLongIntHashMap(Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR, -1, noEntryValue);
		this.noEntryValue = noEntryValue;
	}

	@Override
	public RandomAccess<IntType> randomAccess() {
		return new MyRandomAccess();
	}

	@Override
	public RandomAccess<IntType> randomAccess(Interval interval) {
		return randomAccess();
	}

	public Cursor<IntType> sparseCursor() {
		return new MappingCursor<>(sparsityPattern().cursor(), randomAccess());
	}

	public IterableRegion<? extends BooleanType<?>> sparsityPattern() {
		return new SparseIterableRegion(this, values.keySet());
	}

	// -- Helper methods --

	public void clear() {
		values.clear();
	}

	private int get(MyRandomAccess position) {
		long index = indexer.positionToIndex(position);
		while (true) {
			try {
				long readId = lock.startRead();
				int value = values.get(index);
				if (lock.isReadValid(readId))
					return value;
			}
			catch (ArrayIndexOutOfBoundsException ignore) {
				// NB: TLongInHashMap.get(long) sometimes throws an
				// ArrayIndexOutOfBoundsException, if it is rehashed.
			}
		}
	}

	private void set(MyRandomAccess position, int value) {
		long index = indexer.positionToIndex(position);

		synchronized (lock) {
			lock.writeLock();
			try {
				if (value == noEntryValue)
					values.remove(index);
				else
					values.put(index, value);
			}
			finally {
				lock.writeUnlock();
			}
		}
	}

	// -- Helper classes --

	private class MyRandomAccess extends Point implements RandomAccess<IntType> {

		private IntType value = new IntType(new IntAccess() {

			@Override
			public int getValue(int ignored) {
				return SparseRandomAccessIntType.this.get(MyRandomAccess.this);
			}

			@Override
			public void setValue(int ignored, int value) {
				SparseRandomAccessIntType.this.set(MyRandomAccess.this, value);
			}
		});

		private MyRandomAccess() {
			super(SparseRandomAccessIntType.this.numDimensions());
		}

		private MyRandomAccess(Localizable localizable) {
			super(localizable);
		}

		@Override
		public RandomAccess<IntType> copyRandomAccess() {
			return new MyRandomAccess(this);
		}

		@Override
		public IntType get() {
			return value;
		}

		@Override
		public Sampler<IntType> copy() {
			throw new UnsupportedOperationException();
		}
	}
}
