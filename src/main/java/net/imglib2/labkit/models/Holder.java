package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;

import java.util.function.Consumer;

public interface Holder<T> {

	void set(T value);

	T get();

	Notifier<Consumer<T>> notifier();
}
