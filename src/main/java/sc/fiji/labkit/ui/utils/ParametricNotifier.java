/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.utils;

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
