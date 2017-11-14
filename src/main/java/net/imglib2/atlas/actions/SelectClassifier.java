package net.imglib2.atlas.actions;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.atlas.Extensible;
import net.imglib2.trainable_segmention.RevampUtils;
import net.miginfocom.swing.MigLayout;
import weka.classifiers.Classifier;
import weka.core.PluginManager;
import weka.gui.GenericObjectEditor;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author Matthias Arzt
 */
public class SelectClassifier {

	private final net.imglib2.atlas.classification.Classifier classifier;

	public SelectClassifier(Extensible extensible, net.imglib2.atlas.classification.Classifier classifier) {
		this.classifier = classifier;
		extensible.addAction("Select Classification Algorithm ...", "selectAlgorithm", this::run, "");
	}

	private void run() {
		classifier.editClassifier();
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
