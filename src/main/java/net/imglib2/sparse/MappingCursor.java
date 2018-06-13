
package net.imglib2.sparse;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;

/**
 * @author Matthias Arzt
 */
public class MappingCursor<T> implements Cursor<T> {

	private final Cursor<?> cursor;

	private final RandomAccess<T> randomAccess;

	public MappingCursor(Cursor<?> cursor, RandomAccess<T> randomAccess) {
		this.cursor = cursor;
		this.randomAccess = randomAccess;
	}

	@Override
	public Cursor<T> copyCursor() {
		return new MappingCursor<>(cursor.copyCursor(), randomAccess
			.copyRandomAccess());
	}

	@Override
	public T next() {
		fwd();
		return get();
	}

	@Override
	public void jumpFwd(long steps) {
		cursor.jumpFwd(steps);
		randomAccess.setPosition(cursor);
	}

	@Override
	public void fwd() {
		cursor.fwd();
		randomAccess.setPosition(cursor);
	}

	@Override
	public void reset() {
		cursor.reset();
		randomAccess.setPosition(cursor);
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}

	@Override
	public void localize(int[] position) {
		cursor.localize(position);
	}

	@Override
	public void localize(long[] position) {
		cursor.localize(position);
	}

	@Override
	public int getIntPosition(int d) {
		return cursor.getIntPosition(d);
	}

	@Override
	public long getLongPosition(int d) {
		return cursor.getLongPosition(d);
	}

	@Override
	public void localize(float[] position) {
		cursor.localize(position);
	}

	@Override
	public void localize(double[] position) {
		cursor.localize(position);
	}

	@Override
	public float getFloatPosition(int d) {
		return cursor.getFloatPosition(d);
	}

	@Override
	public double getDoublePosition(int d) {
		return cursor.getDoublePosition(d);
	}

	@Override
	public int numDimensions() {
		return cursor.numDimensions();
	}

	@Override
	public T get() {
		return randomAccess.get();
	}

	@Override
	public Sampler<T> copy() {
		return randomAccess.copy();
	}
}
