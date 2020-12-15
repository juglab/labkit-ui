
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Notifier;

/**
 * An interface that is similar to JavaFX property.
 */
//TODO rename "notifier" to "changeListerners"
public interface Property<T> {

	void set(T value);

	T get();

	Notifier notifier();
}
