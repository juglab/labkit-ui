
package net.imglib2.labkit.utils.properties;

import net.imglib2.labkit.utils.Listeners;

/**
 * A {@link Property}, that stays synchronous with a source properties. It
 * weakly listens to the source properties and can therefore be garbage
 * collected, while the source properties stays in memory.
 */
public class WeakListeningProperty<T> implements Property<T> {

	private final Property<T> source;

	private final Listeners listeners = new Listeners();

	private final Runnable onSourceChanged = this::onSourceChanged;

	public WeakListeningProperty(Property<T> source) {
		this.source = source;
		source.notifier().addWeakListener(onSourceChanged);
	}

	private void onSourceChanged() {
		listeners.notifyListeners();
	}

	@Override
	public void set(T value) {
		source.set(value);
	}

	@Override
	public T get() {
		return source.get();
	}

	@Override
	public Listeners notifier() {
		return listeners;
	}
}
