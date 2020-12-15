
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Notifier;

/**
 * Default implementation of {@link Property}. Somehow similar to JavaFX
 * property. DefaultHolder holds a value, provides a getter and setter and
 * listerners. The listeners a notified whenever the value changes.
 */
public class DefaultProperty<T> implements Property<T> {

	private Notifier notifier = new Notifier();

	private T value;

	public DefaultProperty(T value) {
		this.value = value;
	}

	@Override
	public void set(T value) {
		if (value == this.value) return;
		this.value = value;
		notifier.notifyListeners();
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public Notifier notifier() {
		return notifier;
	}
}
