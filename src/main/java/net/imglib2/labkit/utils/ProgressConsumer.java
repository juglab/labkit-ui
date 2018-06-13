
package net.imglib2.labkit.utils;

public interface ProgressConsumer {

	static ProgressConsumer systemOut() {
		return (i, n) -> System.out.print("Chunk " + i + " of " + n + "\n");
	}

	void showProgress(int stop, int total);
}
