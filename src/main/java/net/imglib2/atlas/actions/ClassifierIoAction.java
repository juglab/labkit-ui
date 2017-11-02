package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.Classifier;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAcion {

	private final Classifier classifier;

	public ClassifierIoAction(MainFrame.Extensible extensible, final Classifier classifier) {
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.classifier = classifier;
		initSaveAction("Save Classifier ...", "saveClassifier", this::save, "");
		initOpenAction("Open Classifier ...", "openClassifier", this::open, "");
	}

	private void save(String filename) throws Exception {
		classifier.saveClassifier(filename, true);
	}

	private void open(String filename) throws Exception {
		classifier.openClassifier(filename);
	}
}
