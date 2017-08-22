package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;

import java.io.IOException;

/**
 * @author Matthias Arzt
 */
public class ClassifierSaveAndLoad extends AbstractSaveAndLoadAction {

	private final Classifier classifier;

	public ClassifierSaveAndLoad(MainFrame.Extensible extensible, final Classifier classifier) {
		super(extensible);
		this.classifier = classifier;
		initAction("Save Classifier", this::save, "ctrl O");
		initAction("Load Classifier", this::load, "ctrl S");
	}

	private void save(String filename) throws Exception {
		classifier.saveClassifier(filename, true);
	}

	private void load(String filename) throws Exception {
		classifier.loadClassifier(filename);
	}
}
