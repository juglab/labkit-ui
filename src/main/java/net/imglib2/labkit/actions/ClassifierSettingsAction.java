
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.panel.GuiUtils;

/**
 * @author Matthias Arzt
 */
public class ClassifierSettingsAction {

	public ClassifierSettingsAction(Extensible extensible,
		Holder<? extends SegmentationItem> selectedSegmenter)
	{
		Runnable action = () -> selectedSegmenter.get().editSettings(extensible
			.dialogParent());
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Classifier Settings ...", 2, item -> item.editSettings(extensible
				.dialogParent()), GuiUtils.loadIcon("gear.png"), null);
	}
}
