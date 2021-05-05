
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Implements the menu items for saving and opening the classifier.
 *
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAction {

	private final Holder<SegmentationItem> selectedSegmenter;

	public ClassifierIoAction(Extensible extensible,
		final Holder<SegmentationItem> selectedSegmenter)
	{
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.selectedSegmenter = selectedSegmenter;
		initSaveAction(SegmentationItem.SEGMENTER_MENU, "Save Classifier ...", 101,
			this::save, "");
		initOpenAction(SegmentationItem.SEGMENTER_MENU, "Open Classifier ...", 100,
			this::open, "");
	}

	private void save(SegmentationItem item, String filename) {
		item.saveModel(filename);
	}

	private void open(SegmentationItem item, String filename) {
		item.openModel(filename);
		selectedSegmenter.set(item);
	}
}
