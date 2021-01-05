
package net.imglib2.labkit.v2;

import net.imglib2.labkit.v2.controller.LabkitController;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

import java.io.IOException;
import java.nio.file.Files;

public class Demo {

	public static void main(String... arg) throws IOException {
		String projectFolder = Files.createTempDirectory("labkit-demo-project").toFile()
			.getAbsolutePath();
		LabkitModel model = new LabkitModel();
		model.setProjectFolder(projectFolder);
		LabkitView view = new LabkitView();
		LabkitController controller = new LabkitController(model, view);
		controller.addImage("/home/arzt/Documents/Datasets/Example/boats.tif");
		controller.addImage("/home/arzt/Documents/Datasets/Example/AuPbSn40-2.jpg");
		controller.addImage("/home/arzt/Documents/Datasets/Example/t1-head.tif");
		controller.showView();
	}
}
