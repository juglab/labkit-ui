
package net.imglib2.labkit.v2;

import net.imglib2.labkit.v2.controller.LabkitController;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

public class Demo {

	public static void main(String... arg) {
		LabkitModel model = new LabkitModel();
		model.getImageModels().add(ImageModel.createForImageFile(
			"/home/arzt/Documents/Datasets/Example/boats.tif"));
		model.getImageModels().add(ImageModel.createForImageFile(
			"/home/arzt/Documents/Datasets/Example/AuPbSn40-2.jpg"));
		model.getImageModels().add(ImageModel.createForImageFile(
			"/home/arzt/Documents/Datasets/Example/t1-head.tif"));
		LabkitView view = new LabkitView(model);
		LabkitController controller = new LabkitController(model, view);
		controller.showView();
	}
}
