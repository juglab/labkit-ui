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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class NotifierTest {

	private int counter = 0;

	private void increaseCounter() {
		counter++;
	}

	@Test
	public void testWeakListener() {
		Notifier notifier = new Notifier();
		counter = 0;
		notifier.addWeakListener(() -> counter++);
		notifier.notifyListeners();
		assertEquals(1, counter);
		System.gc();
		System.gc();
		notifier.notifyListeners();
		assertEquals(1, counter);
	}

	@Test
	public void testLambdaReferences() {
		Runnable a = this::increaseCounter;
		Runnable b = this::increaseCounter;
		assertNotSame(a, b);
		Runnable c = () -> counter++;
		Runnable d = () -> counter++;
		assertNotSame(c, d);
	}

	@Test
	public void testRemoveListener() {
		Notifier notifier = new Notifier();
		counter = 0;
		Runnable increaseCounter = this::increaseCounter;
		notifier.addListener(increaseCounter);
		notifier.removeListener(increaseCounter);
		notifier.notifyListeners();
		assertEquals(0, counter);
	}
}
