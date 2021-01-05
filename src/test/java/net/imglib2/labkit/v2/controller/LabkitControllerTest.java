
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public class LabkitControllerTest {

	@Test
	public void testLabelingFileClash() {
		LabkitModel model = new LabkitModel();
		LabkitController controller = new LabkitController(model, new LabkitView());
		controller.addImage("/path/to/a/image.tif");
		controller.addImage("/path/to/b/image.tif");
		String labelingFileA = model.getImageModels().get(0).getLabelingFile();
		String labelingFileB = model.getImageModels().get(1).getLabelingFile();
		assertNotEquals(labelingFileA, labelingFileB);
	}
}
