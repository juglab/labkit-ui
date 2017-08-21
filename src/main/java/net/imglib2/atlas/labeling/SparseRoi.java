package net.imglib2.atlas.labeling;

import com.google.gson.annotations.JsonAdapter;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import net.imglib2.*;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.IntervalIndexer;

import java.util.Iterator;

/**
 * @author Matthias Arzt
 */
@JsonAdapter(SparseRoiSerializer.Adapter.class)
public class SparseRoi extends AbstractWrappedInterval<Interval> implements IterableRegion<BitType> {

	final private TLongSet positions = new TLongHashSet();

	final private long[] dimensions;

	public SparseRoi(Interval source) {
		super(source);
		dimensions = new long[source.numDimensions()];
		source.dimensions(dimensions);
	}

	@Override
	public Cursor<Void> cursor() {
		return new SparseRoiCursor();
	}

	@Override
	public Cursor<Void> localizingCursor() {
		return cursor();
	}

	@Override
	public long size() {
		return positions.size();
	}

	@Override
	public Void firstElement() {
		return null;
	}

	@Override
	public Object iterationOrder() {
		return null;
	}

	@Override
	public Iterator<Void> iterator() {
		return cursor();
	}

	@Override
	public RandomAccess<BitType> randomAccess() {
		return new SparseRoiRandomAccess();
	}

	@Override
	public RandomAccess<BitType> randomAccess(Interval interval) {
		return randomAccess();
	}

	private void indexToPosition(Long index, long[] position) {
		IntervalIndexer.indexToPosition(index, dimensions, position);
	}

	private Long positionToIndex(long[] position) {
		return IntervalIndexer.positionToIndex(position, dimensions);
	}

	private class SparseRoiCursor extends AbstractCursor<Void> implements Cursor<Void> {

		private TLongIterator indices;

		private long index;

		private long[] position;

		private SparseRoiCursor() {
			super(SparseRoi.this.numDimensions());
			reset();
			position = new long[numDimensions()];
		}

		@Override
		public Void get() {
			return null;
		}

		@Override
		public AbstractCursor<Void> copy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public AbstractCursor<Void> copyCursor() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void fwd() {
			index = indices.next();
		}

		@Override
		public void reset() {
			indices = positions.iterator();
		}

		@Override
		public boolean hasNext() {
			return indices.hasNext();
		}

		@Override
		public void localize(long[] position) {
			indexToPosition(index, position);
		}

		@Override
		public long getLongPosition(int d) {
			localize(position);
			return position[d];
		}
	}

	private class SparseRoiRandomAccess extends Point implements RandomAccess<BitType> {

		private BitType value = new BitType(false) {
			@Override
			public void set(boolean value) {
				if(value)
					positions.add(positionToIndex(position));
				else
					positions.remove(positionToIndex(position));
			}

			@Override
			public boolean get() {
				return positions.contains(positionToIndex(position));
			}
		};

		private SparseRoiRandomAccess() {
			super(SparseRoi.this.numDimensions());
		}

		private SparseRoiRandomAccess(Localizable localizable) {
			super(localizable);
		}

		@Override
		public RandomAccess<BitType> copyRandomAccess() {
			return new SparseRoiRandomAccess(this);
		}

		@Override
		public BitType get() {
			return value;
		}

		@Override
		public Sampler<BitType> copy() {
			throw new UnsupportedOperationException();
		}
	}
}
