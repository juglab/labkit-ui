
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Listeners;

/**
 * Default implementation of {@link Property}. Somehow similar to JavaFX
 * property. DefaultHolder holds a value, provides a getter and setter and
 * listerners. The listeners a notified whenever the value changes.
 */
public class DefaultProperty<T> implements Property<T> {

	private Listeners listeners = new Listeners();

	private T value;

	public DefaultProperty(T value) {
		this.value = value;
	}

	@Override
	public void set(T value) {
		if (value == this.value) return;
		this.value = value;
		listeners.notifyListeners();
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public Listeners notifier() {
		return listeners;
	}
}
