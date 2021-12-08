
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPluginService;
import org.scijava.Context;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Implements the menu items for saving and opening the classifier.
 *
 * @author Matthias Arzt
 */
public class ClassifierIoAction extends AbstractFileIoAction {

	private final SegmenterListModel segmenterListModel;

	private final Context context;

	public ClassifierIoAction(Extensible extensible,
		final SegmenterListModel selectedSegmenter)
	{
		super(extensible, new FileNameExtensionFilter("Classifier", "classifier"));
		this.context = extensible.context();
		this.segmenterListModel = selectedSegmenter;
		initSaveAction(SegmentationItem.SEGMENTER_MENU, "Save Classifier ...", 101,
			this::save, "");
		initOpenAction(SegmentationItem.SEGMENTER_MENU, "Open Classifier ...", 100,
			this::open, "");
	}

	private void save(SegmentationItem item, String filename) {
		item.saveModel(filename);
	}

	private void open(SegmentationItem item, String filename) {
		item = addMatchingSegmentationItem(filename);
		item.openModel(filename);
		segmenterListModel.selectedSegmenter().set(item);
	}

	private SegmentationItem addMatchingSegmentationItem(String filename) {
		SegmentationPluginService pluginService = context.service(SegmentationPluginService.class);
		for (SegmentationPlugin plugin : pluginService.getSegmentationPlugins())
			if (plugin.canOpenFile(filename))
				return segmenterListModel.addSegmenter(plugin);
		throw new IllegalArgumentException("No suitable plugin found for opening: \"" + filename +
			"\"");
	}
}
