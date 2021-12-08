
package sc.fiji.labkit.ui.models;

import sc.fiji.labkit.ui.utils.Notifier;

/**
 * An interface that is similar to JavaFX property.
 */
//TODO rename "notifier" to "changeListerners"
public interface Holder<T> {

	void set(T value);

	T get();

	Notifier notifier();
}
