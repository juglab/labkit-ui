
package sc.fiji.labkit.ui.models;

import sc.fiji.labkit.ui.utils.Notifier;

/**
 * Default implementation of {@link Holder}. Somehow similar to JavaFX property.
 * DefaultHolder holds a value, provides a getter and setter and listerners. The
 * listeners a notified whenever the value changes.
 */
public class DefaultHolder<T> implements Holder<T> {

	private Notifier notifier = new Notifier();

	private T value;

	public DefaultHolder(T value) {
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
