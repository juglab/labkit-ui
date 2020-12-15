
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Listeners;

import java.util.function.Function;

/**
 * A {@link Property} of which the value derives from a given source Holder, to
 * which an operation is applied.
 * <p>
 * Provides only readonly functionality.
 */
public class MappedProperty<T, R> implements Property<R> {

	private final Property<T> source;

	private final Function<T, R> operation;

	public MappedProperty(Property<T> source, Function<T, R> operation) {
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
	public Listeners notifier() {
		return source.notifier();
	}
}
