package net.imglib2.labkit;

import java.util.function.Consumer;

public interface Holder<T> {

	void set(T value);

	T get();

	Notifier<Consumer<T>> notifier();
}
