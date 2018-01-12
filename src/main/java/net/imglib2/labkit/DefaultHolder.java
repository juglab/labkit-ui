package net.imglib2.labkit;

import java.util.function.Consumer;

/**
 * Created by arzt on 21.08.17.
 */
public class DefaultHolder<T> implements Holder<T> {

	private Notifier<Consumer<T>> notifier = new Notifier<>();

	private T value;

	public DefaultHolder(T value) {
		this.value = value;
	}

	@Override
	public void set(T value) {
		if(value == this.value)
			return;
		this.value = value;
		notifier.forEach(listener -> listener.accept(value));
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public Notifier<Consumer<T>> notifier() {
		return notifier;
	}
}
