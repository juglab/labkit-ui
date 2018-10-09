
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.TransformationModel;

/**
 * @author Matthias Arzt
 */
public class ResetViewAction {

	public ResetViewAction(Extensible extensible, ImageLabelingModel model) {
		Runnable action = () -> {
			TransformationModel transformationModel = model.transformationModel();
			transformationModel.transformToShowInterval(model.labeling().get()
				.interval(), model.labelTransformation());
		};
		extensible.addMenuItem(MenuBar.VIEW_MENU, "Reset View", 100,
			ignore -> action.run(), null, "");
	}
}
