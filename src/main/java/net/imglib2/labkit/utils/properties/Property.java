
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Listeners;

/**
 * An interface that is similar to JavaFX property.
 */
//TODO rename "notifier" to "changeListerners"
public interface Property<T> {

	void set(T value);

	T get();

	Listeners notifier();
}
