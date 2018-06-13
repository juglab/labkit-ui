
package net.imglib2.labkit.utils;

import java.util.HashSet;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;

public class RandomAccessibleContainer<T> implements RandomAccessible<T> {

	private RandomAccessible<T> source;

	public RandomAccessibleContainer(final RandomAccessible<T> source) {
		super();
		this.source = source;
	}

	public void setSource(final RandomAccessible<T> source) {
		this.source = source;
	}

	@Override
	public RandomAccess<T> randomAccess() {
		return source.randomAccess();
	}

	@Override
	public RandomAccess<T> randomAccess(final Interval interval) {
		return source.randomAccess(interval);
	}

	@Override
	public int numDimensions() {
		return source.numDimensions();
	}

}
