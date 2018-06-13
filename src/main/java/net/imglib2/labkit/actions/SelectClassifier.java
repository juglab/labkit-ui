
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;

/**
 * @author Matthias Arzt
 */
public class SelectClassifier {

	public SelectClassifier(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter)
	{
		extensible.addAction("Segmentation Settings ...", "segmenterSettings",
			() -> selectedSegmenter.get().segmenter().editSettings(extensible
				.dialogParent()), "");
	}
}
