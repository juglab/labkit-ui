
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.TransformationModel;
import net.imglib2.labkit.models.ImageLabelingModel;

/**
 * @author Matthias Arzt
 */
public class ResetViewAction {

	public ResetViewAction(Extensible extensible, ImageLabelingModel model) {
		extensible.addAction("Reset View", "resetView", () -> {
			TransformationModel transformationModel = model.transformationModel();
			transformationModel.transformToShowInterval(model.labeling().get()
				.interval(), model.labelTransformation());
		}, "");
	}
}
