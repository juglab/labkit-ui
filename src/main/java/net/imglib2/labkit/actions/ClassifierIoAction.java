package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.segmentation.Segmenter;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAcion {

	private final Segmenter segmenter;

	public ClassifierIoAction(Extensible extensible, final Segmenter segmenter ) {
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.segmenter = segmenter;
		initSaveAction("Save Classifier ...", "saveClassifier", this::save, "");
		initOpenAction("Open Classifier ...", "openClassifier", this::open, "");
	}

	private void save(String filename) throws Exception {
		segmenter.saveClassifier(filename, true);
	}

	private void open(String filename) throws Exception {
		segmenter.openClassifier(filename);
	}
}
