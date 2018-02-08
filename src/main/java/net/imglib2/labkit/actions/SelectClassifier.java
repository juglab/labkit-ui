package net.imglib2.labkit.actions;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.classification.Segmenter;
import net.miginfocom.swing.MigLayout;
import weka.classifiers.Classifier;
import weka.gui.GenericObjectEditor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Matthias Arzt
 */
public class SelectClassifier {

	private final Segmenter segmenter;

	public SelectClassifier(Extensible extensible, Segmenter segmenter ) {
		this.segmenter = segmenter;
		extensible.addAction("Select Classification Algorithm ...", "selectAlgorithm", this::run, "");
	}

	private void run() {
		segmenter.editClassifier();
	}

	public static Classifier runStatic(Component dialogParent, Classifier defaultValue) {
		JCheckBox checkBox = new JCheckBox("FastRandomForest");
		GenericObjectEditor editor = new GenericObjectEditor();
		editor.setClassType(Classifier.class);
		if(defaultValue instanceof FastRandomForest)
			checkBox.setSelected(true);
		else
			editor.setValue(defaultValue);
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(checkBox, "wrap");
		panel.add(editor.getCustomPanel());
		JOptionPane.showMessageDialog(dialogParent, panel, "Select Classification Algorithm", JOptionPane.PLAIN_MESSAGE);
		return checkBox.isSelected() ? new FastRandomForest() : (Classifier) editor.getValue();
	}

	public static void main(String... args) {
		runStatic(null, new FastRandomForest());
	}
}
