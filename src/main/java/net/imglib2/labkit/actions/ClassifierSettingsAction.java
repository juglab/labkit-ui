
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.panel.GuiUtils;

import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class ClassifierSettingsAction {

	public ClassifierSettingsAction(Extensible extensible, SegmenterListModel segmenterListModel) {
		Consumer<SegmentationItem> action = i -> i.editSettings(extensible.dialogParent(),
			segmenterListModel.trainingData().get());
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Classifier Settings ...", 2, action, GuiUtils.loadIcon("gear.png"), null);
	}
}
