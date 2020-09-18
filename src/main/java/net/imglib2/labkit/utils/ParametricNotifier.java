
package net.imglib2.labkit.utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ParametricNotifier<T> {

	private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

	private final List<Reference<? extends Consumer<T>>> weakListeners = new CopyOnWriteArrayList<>();

	private final ReferenceQueue<Consumer<T>> queue = new ReferenceQueue<>();

	public void notifyListeners(T value) {
		listeners.forEach(listener -> listener.accept(value));
		cleanWeakListeners();
		weakListeners.forEach(reference -> {
			Consumer<T> listener = reference.get();
			if (listener != null)
				listener.accept(value);
		});
	}

	public void addListener(Consumer<T> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<T> listener) {
		listeners.remove(listener);
	}

	public void addWeakListener(Consumer<T> listener) {
		cleanWeakListeners();
		weakListeners.add(new WeakReference<>(listener, queue));
	}

	private void cleanWeakListeners() {
		while (true) {
			Reference<? extends Consumer<T>> reference = queue.poll();
			if (reference == null)
				break;
			else
				weakListeners.remove(reference);
		}
	}

	public void removeWeakListener(Consumer<T> listener) {
		weakListeners.removeIf(reference -> {
			Consumer<T> value = reference.get();
			return value == null || value == listener;
		});
	}
}
