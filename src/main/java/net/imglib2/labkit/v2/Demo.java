
package net.imglib2.labkit.v2;

import net.imglib2.labkit.v2.controller.LabkitController;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

public class Demo {

	public static void main(String... arg) {
		LabkitModel model = new LabkitModel();
		model.getImageModels().add(ImageModel.createForImageFile("a.tif"));
		model.getImageModels().add(ImageModel.createForImageFile("b.tif"));
		model.getImageModels().add(ImageModel.createForImageFile("c.tif"));
		LabkitView view = new LabkitView(model);
		LabkitController controller = new LabkitController(model, view);
		controller.showView();
	}
}
