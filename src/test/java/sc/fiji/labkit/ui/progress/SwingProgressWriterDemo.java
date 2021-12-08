
package sc.fiji.labkit.ui.progress;

import bdv.export.ProgressWriter;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;

public class SwingProgressWriterDemo {

	public static void main(String... args) throws InterruptedException {
		ProgressWriter progressWriter = new SwingProgressWriter(null,
			"Swing Progress Writer Demo");
		for (double progress = 0; progress < 1.0; progress += 0.01) {
			if (progress > 0.5) progressWriter.out().println("half done");
			progressWriter.setProgress(progress);
			Thread.sleep(50);
		}
		progressWriter.setProgress(1.0);
	}
}
