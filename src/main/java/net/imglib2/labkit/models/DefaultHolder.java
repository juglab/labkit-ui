
package net.imglib2.labkit.models;

import net.imglib2.labkit.utils.Notifier;

/**
 * Created by arzt on 21.08.17.
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
