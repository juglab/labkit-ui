
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;

public interface Holder<T> {

	void set(T value);

	T get();

	Notifier notifier();
}
