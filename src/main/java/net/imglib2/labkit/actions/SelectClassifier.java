package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.classification.Segmenter;

/**
 * @author Matthias Arzt
 */
public class SelectClassifier {

	public SelectClassifier(Extensible extensible, Segmenter segmenter ) {
		extensible.addAction("Select Classification Algorithm ...",
				"selectAlgorithm",
				() -> segmenter.editSettings(extensible.dialogParent()), "");
	}
}
