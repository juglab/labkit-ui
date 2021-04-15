
package net.imglib2.labkit.denoiseg;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.util.function.IntConsumer;

public class DenoiSegParametersDialog extends JFrame {

	public DenoiSegParametersDialog(final DenoiSegParameters params, int[] dims) {
		initComponents(params, dims);
	}

	private void initComponents(final DenoiSegParameters params, int[] dims) {
		setLayout(new MigLayout("", "[grow][grow, fill]", ""));
		addSpinners(params, dims);
		addDoneButton();
	}

	private void addSpinners(final DenoiSegParameters params, int[] dims) {

		int depth = dims[2];
		int minHW = Math.min(dims[0], dims[1]);

		// components

		addLabel("Number of epochs:");
		addSpinner(params.getNumEpochs(), params::setNumEpochs, 1, 10000, 1);
		addLabel("Number of steps per epochs:");
		addSpinner(params.getNumStepsPerEpoch(), params::setNumStepsPerEpoch, 1, 10000, 1);
		addLabel("Batch size:");
		addSpinner(Math.min(64, params.getBatchSize()), params::setBatchSize, 1, depth, 1);
		addLabel("Patch shape:");
		addSpinner(params.getPatchShape(), params::setPatchShape, 16, 512, 16);
		// TODO: make sure this is correct
		addLabel("Neighborhood radius:");
		addSpinner(params.getNeighborhoodRadius(), params::setNeighborhoodRadius, 1, minHW, 1);
		addLabel("Labeled image % used for validation:");
		addSpinner(params.getValidationPercentage(), params::setValidationPercentage, 1, 99, 1);
	}

	private void addLabel(String s) {
		add(new JLabel(s));
	}

	private void addSpinner(int initialValue, IntConsumer setter, int min, int max, int step) {
		SpinnerModel epochModel = new SpinnerNumberModel(initialValue, min, max, step);
		JSpinner epochsSpinner = new JSpinner(epochModel);
		epochsSpinner.addChangeListener(e -> {
			int val = (int) epochsSpinner.getValue();
			setter.accept(val);
		});
		add(epochsSpinner, "wrap");
	}

	private void addDoneButton() {
		JButton button = new JButton("Done");
		button.addActionListener(e -> dispose());
		add(button);
	}

	public static void main(final String... args) {
		JFrame dialog = new DenoiSegParametersDialog(new DenoiSegParameters(),
			new int[] { 200, 300, 70 });
		dialog.pack();
		dialog.setVisible(true);
	}
}
