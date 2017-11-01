package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.Classifier;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Matthias Arzt
 */
public class ClassifierSaveAndLoad extends AbstractSaveAndLoadAction {

	private final Classifier classifier;

	public ClassifierSaveAndLoad(MainFrame.Extensible extensible, final Classifier classifier) {
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.classifier = classifier;
		initSaveAction("Save Classifier ...", "saveClassifier", this::save, "");
		initLoadAction("Load Classifier ...", "loadClassifier", this::load, "");
	}

	private void save(String filename) throws Exception {
		classifier.saveClassifier(filename, true);
	}

	private void load(String filename) throws Exception {
		classifier.loadClassifier(filename);
	}
}
