/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.utils.progress;

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
		SwingUtilities.invokeLater(() -> {
			if (!line.isEmpty()) dialog.setNote(line);
			dialog.addDetails(line);
		});
	}

	@Override
	public void setProgress(double completionRatio) {
		SwingUtilities.invokeLater(() -> {
			dialog.setProgress(completionRatio);
		});
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
