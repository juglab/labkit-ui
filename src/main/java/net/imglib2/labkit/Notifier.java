package net.imglib2.labkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class Notifier<T> {

	private final List<T> listeners = new CopyOnWriteArrayList<>();

	public void forEach(Consumer<? super T> runnable) {
		listeners.forEach(runnable);
	}

	public void add(T listener) {
		listeners.add(listener);
	}

	public void remove(T listener) {
		listeners.remove(listener);
	}
}
