
package net.imglib2.labkit.utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Matthias Arzt
 */
public class Listeners {

	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

	private final List<Reference<? extends Runnable>> weakListeners = new CopyOnWriteArrayList<>();

	private final ReferenceQueue<Runnable> queue = new ReferenceQueue<>();

	public void notifyListeners() {
		listeners.forEach(Runnable::run);
		cleanWeakListeners();
		weakListeners.forEach(reference -> {
			Runnable runnable = reference.get();
			if (runnable != null)
				runnable.run();
		});
	}

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	public void removeListener(Runnable listener) {
		listeners.remove(listener);
	}

	public void addWeakListener(Runnable listener) {
		cleanWeakListeners();
		weakListeners.add(new WeakReference<>(listener, queue));
	}

	private void cleanWeakListeners() {
		while (true) {
			Reference<? extends Runnable> reference = queue.poll();
			if (reference == null)
				break;
			else
				weakListeners.remove(reference);
		}
	}

	public void removeWeakListener(Runnable listener) {
		weakListeners.removeIf(reference -> {
			Runnable value = reference.get();
			return value == null || value == listener;
		});
	}
}
