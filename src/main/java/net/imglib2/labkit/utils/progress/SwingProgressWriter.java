
package net.imglib2.labkit.utils.progress;

import bdv.export.ProgressWriter;

import javax.swing.*;
import java.io.PrintStream;
import java.util.concurrent.CancellationException;

public class SwingProgressWriter implements ProgressWriter {

	private final ProgressDialog dialog;
	private final PrintStream out = PrintStreamToLines.printStreamToLines(
		this::showNote);
	private final PrintStream err = PrintStreamToLines.printStreamToLines(
		this::showNote);

	public SwingProgressWriter(JFrame dialogParent, String title) {
		dialog = new ProgressDialog(dialogParent, title);
	}

	@Override
	public PrintStream out() {
		return out;
	}

	@Override
	public PrintStream err() {
		return err;
	}

	private void showNote(String line) {
		if (!line.isEmpty()) dialog.setNote(line);
		dialog.addDetails(line);
	}

	@Override
	public void setProgress(double completionRatio) {
		dialog.setProgress(completionRatio);
		if (dialog.isCanceled()) throw new CancellationException();
	}

	public void setVisible(boolean visible) {
		dialog.setVisible(visible);
	}

	public void setProgressBarVisible(boolean visible) {
		dialog.setProgressBarVisible(visible);
	}

	public void setDetailsVisible(boolean visible) {
		dialog.setDetailsVisible(visible);
	}

	public static void main(String... args) throws InterruptedException {
		SwingProgressWriter pw = new SwingProgressWriter(null,
			"SwingProgressWriter Demo");
		int steps = 30;
		for (int i = 0; i < steps; i++) {
			pw.setProgress((double) i / steps);
			pw.out().println("step " + i + " of " + steps);
			Thread.sleep(1000);
		}
		pw.setProgress(1.0);
	}
}
