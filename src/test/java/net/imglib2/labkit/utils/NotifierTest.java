
package net.imglib2.labkit.utils;

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
		Listeners listeners = new Listeners();
		counter = 0;
		listeners.addWeakListener(() -> counter++);
		listeners.notifyListeners();
		assertEquals(1, counter);
		System.gc();
		System.gc();
		listeners.notifyListeners();
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
		Listeners listeners = new Listeners();
		counter = 0;
		Runnable increaseCounter = this::increaseCounter;
		listeners.addListener(increaseCounter);
		listeners.removeListener(increaseCounter);
		listeners.notifyListeners();
		assertEquals(0, counter);
	}
}
