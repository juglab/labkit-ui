
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
		extensible.addAction("Classifier Settings ...", "segmenterSettings",
			() -> selectedSegmenter.get().segmenter().editSettings(extensible
				.dialogParent()), "");
		extensible.addSegmenterMenuItem("Classifier Settings ...", item -> item.segmenter().editSettings(extensible.dialogParent()), GuiUtils.loadIcon("gear.png"));
	}
}
