
package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.panel.GuiUtils;

import java.util.function.Consumer;

/**
 * Implements the menu item for changing the classifier settings.
 *
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
