
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAction {

	private final Holder<SegmentationItem> selectedSegmenter;

	public ClassifierIoAction(Extensible extensible,
		final Holder<SegmentationItem> selectedSegmenter)
	{
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.selectedSegmenter = selectedSegmenter;
		initSaveAction("Save Classifier ...", "saveClassifier", this::save, "");
		initOpenAction("Open Classifier ...", "openClassifier", this::open, "");
	}

	private void save(String filename) throws Exception {
		selectedSegmenter.get().segmenter().saveModel(filename, true);
	}

	private void open(String filename) throws Exception {
		selectedSegmenter.get().segmenter().openModel(filename);
	}
}
