
package net.imglib2.sparse;

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
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

public class DifferenceRandomAccessibleIntType extends
	AbstractWrappedInterval<RandomAccessibleInterval<? extends IntegerType<?>>>
	implements RandomAccessibleInterval<IntType>
{

	private final TLongIntHashMap values;

	public DifferenceRandomAccessibleIntType(
		final RandomAccessibleInterval<? extends IntegerType<?>> source)
	{
		super(source);
		values = new TLongIntHashMap();
	}

	@Override
	public RandomAccess<IntType> randomAccess() {
		return new DifferenceRandomAccess();
	}

	@Override
	public RandomAccess<IntType> randomAccess(final Interval interval) {
		return new DifferenceRandomAccess(interval);
	}

	public IterableRegion<? extends BooleanType<?>> differencePattern() {
		return new SparseIterableRegion(this, values.keySet());
	}

	private class DifferenceRandomAccess extends Point implements
		RandomAccess<IntType>
	{

		private final IntType value = new IntType(new IntAccess() {

			@Override
			public int getValue(final int ignored) {
				final long index = IntervalIndexer.positionToIndex(
					DifferenceRandomAccess.this, sourceInterval);
				if (values.containsKey(index)) {
					return values.get(index);
				}
				else {
					sourceRA.setPosition(DifferenceRandomAccess.this);
					return sourceRA.get().getInteger();
				}
			}

			@Override
			public void setValue(final int ignored, final int value) {
				final long index = IntervalIndexer.positionToIndex(
					DifferenceRandomAccess.this, sourceInterval);
				sourceRA.setPosition(DifferenceRandomAccess.this);
				if (sourceRA.get().getInteger() == value) {
					values.remove(index);
				}
				else {
					values.put(index, value);
				}
			}
		});

		private final RandomAccess<? extends IntegerType<?>> sourceRA;

		private DifferenceRandomAccess() {
			super(sourceInterval.numDimensions());
			sourceRA = sourceInterval.randomAccess();
		}

		private DifferenceRandomAccess(final Interval interval) {
			super(sourceInterval.numDimensions());
			sourceRA = sourceInterval.randomAccess(interval);
		}

		private DifferenceRandomAccess(final Localizable localizable) {
			super(localizable);
			sourceRA = sourceInterval.randomAccess();
		}

		@Override
		public RandomAccess<IntType> copyRandomAccess() {
			return new DifferenceRandomAccess(this);
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
