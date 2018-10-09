
package net.imglib2.labkit.actions;

import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
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
		Runnable action = () -> selectedSegmenter.get().segmenter().editSettings(
			extensible.dialogParent());
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU, "Classifier Settings ...", 2,
			ignore -> action.run(), null, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Classifier Settings ...", 2, item -> item.segmenter().editSettings(
				extensible.dialogParent()), GuiUtils.loadIcon("gear.png"), null);
	}
}
