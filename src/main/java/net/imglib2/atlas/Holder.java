package net.imglib2.atlas;

import java.util.function.Consumer;

/**
 * Created by arzt on 21.08.17.
 */
public class Holder<T> {

	private Notifier<Consumer<T>> notifier = new Notifier<>();

	private T value;

	public Holder(T value) {
		this.value = value;
	}

	public void set(T value) {
		this.value = value;
		notifier.forEach(listener -> listener.accept(value));
	}

	public T get() {
		return value;
	}

	public Notifier<Consumer<T>> notifier() {
		return notifier;
	}
}
