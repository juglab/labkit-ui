package net.imglib2.sparse;

/**
 * @author Matthias Arzt
 */
public class StopWatch {
	private final long startTime = System.nanoTime();

	public long timeInMilliSeconds() {
		final long stopTime = System.nanoTime();
		return (stopTime - startTime) / 1000000;
	}

	@Override
	public String toString() {
		return timeInMilliSeconds() + " ms";
	}
}
