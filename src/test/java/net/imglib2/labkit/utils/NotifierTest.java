
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
		notifier.addListener(this::increaseCounter);
		notifier.removeListener(this::increaseCounter);
		notifier.notifyListeners();
		assertEquals(0, counter);
	}
}
