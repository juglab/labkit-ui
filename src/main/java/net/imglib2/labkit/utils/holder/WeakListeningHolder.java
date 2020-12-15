
package net.imglib2.labkit.utils.holder;

import net.imglib2.labkit.utils.Notifier;

/**
 * A {@link Holder}, that stays synchronous with a source holder. It weakly
 * listens to the source holder and can therefore be garbage collected, while
 * the source holder stays in memory.
 */
public class WeakListeningHolder<T> implements Holder<T> {

	private final Holder<T> source;

	private final Notifier listeners = new Notifier();

	private final Runnable onSourceChanged = this::onSourceChanged;

	public WeakListeningHolder(Holder<T> source) {
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
	public Notifier notifier() {
		return listeners;
	}
}
