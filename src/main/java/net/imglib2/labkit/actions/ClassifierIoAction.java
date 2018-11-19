
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
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
		initSaveAction(MenuBar.SEGMENTER_MENU, "Save Classifier ...", 101,
			this::save, "");
		initOpenAction(MenuBar.SEGMENTER_MENU, "Open Classifier ...", 100,
			this::open, "");
	}

	private void save(String filename) {
		selectedSegmenter.get().segmenter().saveModel(filename);
	}

	private void open(String filename) {
		selectedSegmenter.get().segmenter().openModel(filename);
	}
}
