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

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.time.Duration;

/**
 * A dialog the shows a progress bar.
 */
public class ProgressDialog {

	private final JDialog dialog;
	private final JLabel note = new JLabel("");
	private final JProgressBar progressBar = new JProgressBar(0, 1000);
	private final DetailsPane details = new DetailsPane();
	private long start;
	private boolean canceled = false;
	private boolean hide = false;

	public ProgressDialog(Frame parent, String text) {
		JOptionPane pane = new JOptionPane(new Object[] { text, note, progressBar,
			details }, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
			null, new Object[] { "Hide", "Cancel" });
		pane.addPropertyChangeListener(this::buttonClicked);
		dialog = pane.createDialog(parent, text);
		dialog.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				hide = true;
			}
		});
		dialog.setResizable(true);
		dialog.setModal(false);
		dialog.add(pane);
		dialog.pack();
		progressBar.setStringPainted(true);
		start = System.currentTimeMillis();
	}

	private void buttonClicked(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
			Object title = event.getNewValue();
			if ("Cancel".equals(title)) canceled = true;
			if ("Hide".equals(title)) hide = true;
		}
	}

	public void setNote(String note) {
		this.note.setText(note);
	}

	public void setProgress(double progress) {
		progressBar.getModel().setValue((int) (progress * 1000));
		String duration = timeEstimateAsString(progress);
		progressBar.setString(String.format("%.1f %%", progress * 100) + duration);
		setVisible(!canceled && !hide && progress < 1.0);
	}

	private String timeEstimateAsString(double progress) {
		if (progress <= 0) {
			start = System.currentTimeMillis();
			return "";
		}
		long seconds = (long) ((System.currentTimeMillis() - start) / 1000. / progress *
			(1 - progress));
		if (seconds < 60)
			return "";
		long mins = seconds / 60;
		long hours = mins / 60;
		String result = String.format("%2d s", seconds % 60);
		if (mins > 0)
			result = String.format("%2d min %s", mins % 60, result);
		if (hours > 0)
			result = String.format("%2d h %s", hours, result);
		return "   " + result;
	}

	public void setVisible(boolean visible) {
		if (dialog.isVisible() == visible)
			return;
		if (visible)
			dialog.setVisible(true);
		else
			dialog.dispose(); // Dispose to allow garbage collection
	}

	public void addDetails(String line) {
		details.add(line);
	}

	public static void main(String... args) throws InterruptedException {
		ProgressDialog dialog = new ProgressDialog(null,
			"Demonstrate ProgressDialog");
		int steps = 20;
		for (int i = 0; i < steps; i++) {
			dialog.setProgress((double) i / steps);
			dialog.setNote("Step " + i + " of " + steps);
			Thread.sleep(1000);
			dialog.addDetails("Step " + i + " completed\n");
		}
		dialog.setProgress(1.0);
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setProgressBarVisible(boolean visible) {
		progressBar.setVisible(visible);
	}

	public void setDetailsVisible(boolean visible) {
		details.setVisible(visible);
	}

	private static class DetailsPane extends JPanel {

		private final JTextArea text;

		DetailsPane() {
			text = new JTextArea();
			text.setBackground(Color.BLACK);
			text.setForeground(Color.LIGHT_GRAY);
			text.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
			final JScrollPane scrollPane = new JScrollPane(text);
			scrollPane.setVisible(false);
			setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));
			final JCheckBox show_details = new JCheckBox("show Details");
			show_details.addItemListener(event -> {
				scrollPane.setVisible(event.getStateChange() == ItemEvent.SELECTED);
			});
			add(show_details, "wrap");
			add(scrollPane, "grow");
		}

		public void add(String line) {
			final Document document = text.getDocument();
			try {
				document.insertString(document.getLength(), line + "\n", null);
			}
			catch (BadLocationException e) {}
		}
	}
}
