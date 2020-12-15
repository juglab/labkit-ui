
package net.imglib2.labkit.utils.holder;

import net.imglib2.labkit.utils.Notifier;

/**
 * An interface that is similar to JavaFX property.
 */
//TODO rename "notifier" to "changeListerners"
public interface Holder<T> {

	void set(T value);

	T get();

	Notifier notifier();
}
