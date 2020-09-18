
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;

import java.util.function.Function;

/**
 * A {@link Holder} of which the value derives from a given source Holder, to
 * which an operation is applied.
 * <p>
 * Provides only readonly functionality.
 */
public class MappedHolder<T, R> implements Holder<R> {

	private final Holder<T> source;

	private final Function<T, R> operation;

	public MappedHolder(Holder<T> source, Function<T, R> operation) {
		this.source = source;
		this.operation = operation;
	}

	@Override
	public void set(R value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public R get() {
		return operation.apply(source.get());
	}

	@Override
	public Notifier notifier() {
		return source.notifier();
	}
}
