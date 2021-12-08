
package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.TransformationModel;

/**
 * Implements the reset view menu item.
 *
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
