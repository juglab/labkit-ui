
package net.imglib2.labkit.v2;

import net.imglib2.labkit.v2.controller.LabkitController;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

public class Demo {

	public static void main(String... arg) {
		LabkitModel model = new LabkitModel();
		LabkitView view = new LabkitView();
		LabkitController controller = new LabkitController(model, view);
		controller.showView();
	}
}
