
package net.imglib2.hdf5;

import bdv.export.ProgressWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A {@link ProgressWriter} which does nothing.
 */
public class DummyProgressWriter implements ProgressWriter {

	public final PrintStream dummyPrintStream = new PrintStream(
		new OutputStream()
		{

			@Override
			public void write(int i) throws IOException {
				// do nothing
			}
		});

	@Override
	public PrintStream out() {
		return dummyPrintStream;
	}

	@Override
	public PrintStream err() {
		return dummyPrintStream;
	}

	@Override
	public void setProgress(double completionRatio) {
		// do nothing
	}
}
