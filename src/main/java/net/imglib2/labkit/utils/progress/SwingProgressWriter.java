
package net.imglib2.labkit.utils.progress;

import bdv.export.ProgressWriter;
import net.imglib2.labkit.utils.ProgressConsumer;

import javax.swing.*;
import java.io.PrintStream;
import java.util.concurrent.CancellationException;

public class SwingProgressWriter implements ProgressWriter, ProgressConsumer {

	private final ProgressMonitor dialog;
	private final PrintStream out = PrintStreamToLines.printStreamToLines(
		this::showNote);
	private final PrintStream err = PrintStreamToLines.printStreamToLines(
		this::showNote);

	public SwingProgressWriter(JFrame dialogParent, String title) {
		dialog = new ProgressMonitor(dialogParent, title, null, 0, 1000);
		dialog.setMillisToPopup(0);
		dialog.setMillisToDecideToPopup(0);
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
	}

	@Override
	public void setProgress(double completionRatio) {
		dialog.setProgress((int) (completionRatio * 1000));
		if (dialog.isCanceled()) throw new CancellationException();
	}

	@Override
	public void showProgress(int stop, int total) {
		setProgress((double) stop / (double) total);
	}
}
