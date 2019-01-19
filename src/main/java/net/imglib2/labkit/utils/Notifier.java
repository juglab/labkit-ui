
package net.imglib2.labkit.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class Notifier {

	private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

	public void notifyListeners() {
		listeners.forEach(Runnable::run);
	}

	public void add(Runnable listener) {
		listeners.add(listener);
	}

	public void remove(Runnable listener) {
		listeners.remove(listener);
	}
}
