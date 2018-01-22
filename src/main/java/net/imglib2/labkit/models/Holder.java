package net.imglib2.labkit.models;

import net.imglib2.labkit.Notifier;

import java.util.function.Consumer;

public interface Holder<T> {

	void set(T value);

	T get();

	Notifier<Consumer<T>> notifier();
}
