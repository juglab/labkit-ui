
package sc.fiji.labkit.ui.utils.progress;

import bdv.export.ProgressWriter;
import org.scijava.app.StatusService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class StatusServiceProgressWriter implements ProgressWriter {

	private final StatusService service;

	public StatusServiceProgressWriter(StatusService service) {
		this.service = service;
	}

	@Override
	public PrintStream out() {
		return dummy();
	}

	@Override
	public PrintStream err() {
		return dummy();
	}

	private PrintStream dummy() {
		return new PrintStream(new OutputStream() {

			@Override
			public void write(int b) throws IOException {

			}
		});
	}

	@Override
	public void setProgress(double completionRatio) {
		service.showProgress((int) (1000 * completionRatio), 1000);
	}
}
