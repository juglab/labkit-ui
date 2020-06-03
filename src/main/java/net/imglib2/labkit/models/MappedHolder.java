
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;

import java.util.function.Function;

public class MappedHolder<T, R> implements Holder<R> {

	private final Holder<T> source;

	private final Function<T, R> opration;

	public MappedHolder(Holder<T> source, Function<T, R> opration) {
		this.source = source;
		this.opration = opration;
	}

	@Override
	public void set(R value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public R get() {
		return opration.apply(source.get());
	}

	@Override
	public Notifier notifier() {
		return source.notifier();
	}
}
