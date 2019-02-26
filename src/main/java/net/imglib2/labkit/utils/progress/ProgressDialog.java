
package net.imglib2.labkit.utils.progress;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;

public class ProgressDialog {

	private final JDialog dialog;
	private final JLabel note = new JLabel("");
	private final JProgressBar progressBar = new JProgressBar(0, 1000);
	private final DetailsPane details = new DetailsPane();
	private boolean canceled = false;
	private boolean hide = false;

	public ProgressDialog(Frame parent, String text) {
		JOptionPane pane = new JOptionPane(new Object[] { text, note, progressBar,
			details }, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
			null, new Object[] { "Hide", "Cancel" });
		pane.addPropertyChangeListener(this::buttonClicked);
		dialog = pane.createDialog(parent, text);
		dialog.setResizable(true);
		dialog.setModal(false);
		dialog.add(pane);
		dialog.pack();
	}

	private void buttonClicked(PropertyChangeEvent event) {
		if (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
			if (event.getNewValue().equals("Cancel")) canceled = true;
			if (event.getNewValue().equals("Hide")) hide = true;
		}
	}

	public void setNote(String note) {
		this.note.setText(note);
	}

	public void setProgress(double progress) {
		progressBar.getModel().setValue((int) (progress * 1000));
		setVisible(!canceled && !hide && progress < 1.0);
	}

	public void setVisible(boolean visible) {
		if (visible) dialog.setVisible(true);
		else dialog.dispose(); // is dispose here to allow garbage collection
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
