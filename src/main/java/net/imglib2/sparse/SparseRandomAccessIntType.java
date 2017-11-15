package net.imglib2.sparse;

import gnu.trove.impl.Constants;
import gnu.trove.impl.hash.THash;
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
public class SparseRandomAccessIntType extends AbstractWrappedInterval<Interval> implements RandomAccessibleInterval<IntType> {

	private final IntervalIndexer2 indexer;
	private final TLongIntHashMap values;
	private final int noEntryValue;

	public SparseRandomAccessIntType(Interval source) {
		this(source, 0);
	}

	public SparseRandomAccessIntType(Interval source, int noEntryValue) {
		super(source);
		this.indexer = new IntervalIndexer2(source);
		this.values = new TLongIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1, noEntryValue);
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

	private int get(MyRandomAccess position) {
		return values.get(indexer.positionToIndex(position));
	}

	private void set(MyRandomAccess position, int value) {
		Long index = indexer.positionToIndex(position);
		if(value == noEntryValue)
			values.remove(index);
		else
			values.put(index, value);
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
