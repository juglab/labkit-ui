package net.imglib2.atlas.actions;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.atlas.MainFrame;
import net.imglib2.trainable_segmention.RevampUtils;
import org.scijava.ui.behaviour.util.RunnableAction;
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

	public SelectClassifier(MainFrame.Extensible extensible, net.imglib2.atlas.classification.Classifier classifier) {
		this.classifier = classifier;
		extensible.addAction(new RunnableAction("Select Classification Algorithm", this::run), "");
	}

	private void run() {
		classifier.editClassifier();
	}

	public static Classifier runStatic(Component dialogParent, Classifier defaultValue) {
		GenericObjectEditor editor = new GenericObjectEditor();
		editor.setClassType(Classifier.class);
		editor.setValue(defaultValue);
		JOptionPane.showMessageDialog(dialogParent, editor.getCustomPanel(), "Select Classification Algorithm", JOptionPane.PLAIN_MESSAGE);
		return (Classifier) editor.getValue();
	}

	public static void main(String... args) {
		runStatic(null, new FastRandomForest());
	}

	static {
		RevampUtils.wrapException(() -> {
			Field field = GenericObjectEditor.class.getDeclaredField("EDITOR_PROPERTIES");
			field.setAccessible(true);
			Properties editorProperties = (Properties)field.get(null);
			String key = "weka.classifiers.Classifier";
			String value = editorProperties.getProperty(key);
			value += ",hr.irb.fastRandomForest.FastRandomForest";
			editorProperties.setProperty(key, value);
			//new Exception("insert").printStackTrace();
			//System.err.println("value: " + value);

			// add classifiers from properties (needed after upgrade to WEKA version 3.7.11)
			PluginManager.addFromProperties(editorProperties);
		});
	}
}
