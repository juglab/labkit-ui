
package net.imglib2.labkit.utils.progress;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class PrintStreamToLines {

	public static PrintStream printStreamToLines(Consumer<String> linesConsumer) {
		return new PrintStream(new OutputStreamToLines(linesConsumer), true);
	}

	private static class OutputStreamToLines extends ByteArrayOutputStream {

		private final Consumer<String> consumer;
		private String prefix = "";

		public OutputStreamToLines(Consumer<String> consumer) {
			this.consumer = consumer;
		}

		@Override
		public synchronized void flush() {
			String text = toString();
			reset();
			process(text);
		}

		private void process(String s) {
			String[] lines = (prefix + s + "\ndummy").split("\n");
			final int lastIndex = lines.length - 2;
			for (int i = 0; i < lastIndex; i++)
				consumer.accept(lines[i]);
			prefix = lines[lastIndex];
		}
	}
}
