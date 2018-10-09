
package net.imglib2.labkit.brush.neighborhood;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;

public final class MappingCursor<T> extends AbstractEuclideanSpace implements
	Cursor<T>
{

	private final RandomAccess<T> source;

	private final Cursor<?> cursor;

	private final long[] offset;

	public MappingCursor(final long offset[], final Cursor<?> cursor,
		final RandomAccess<T> source)
	{
		super(source.numDimensions());
		this.offset = offset;
		this.cursor = cursor;
		this.source = source;
		reset();
	}

	public MappingCursor(MappingCursor<T> mappingCursor) {
		this(mappingCursor.offset, mappingCursor.cursor.copyCursor(),
			mappingCursor.source.copyRandomAccess());
	}

	@Override
	public T get() {
		return source.get();
	}

	@Override
	public void fwd() {
		cursor.fwd();
		source.setPosition(this);
	}

	@Override
	public void jumpFwd(final long steps) {
		cursor.jumpFwd(steps);
		source.setPosition(this);
	}

	@Override
	public T next() {
		fwd();
		return get();
	}

	@Override
	public void remove() {
		// NB: no action.
	}

	@Override
	public void reset() {
		cursor.reset();
		source.setPosition(this);
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}

	@Override
	public float getFloatPosition(final int d) {
		return cursor.getFloatPosition(d) + offset[d];
	}

	@Override
	public double getDoublePosition(final int d) {
		return cursor.getDoublePosition(d) + offset[d];
	}

	@Override
	public int getIntPosition(final int d) {
		return cursor.getIntPosition(d) + (int) offset[d];
	}

	@Override
	public long getLongPosition(final int d) {
		return cursor.getLongPosition(d) + offset[d];
	}

	@Override
	public void localize(final long[] position) {
		cursor.localize(position);
		for (int i = 0; i < offset.length; i++)
			position[i] += offset[i];
	}

	@Override
	public void localize(final float[] position) {
		cursor.localize(position);
		for (int i = 0; i < offset.length; i++)
			position[i] += offset[i];
	}

	@Override
	public void localize(final double[] position) {
		cursor.localize(position);
		for (int i = 0; i < offset.length; i++)
			position[i] += offset[i];
	}

	@Override
	public void localize(final int[] position) {
		cursor.localize(position);
		for (int i = 0; i < offset.length; i++)
			position[i] += offset[i];
	}

	@Override
	public Sampler<T> copy() {
		return source.copy();
	}

	@Override
	public MappingCursor<T> copyCursor() {
		return new MappingCursor<>(this);
	}
}
