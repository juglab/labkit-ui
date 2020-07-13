
package net.imglib2.labkit.multi_image;

import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class LabkitProjectSerializerTest {

	@Test
	public void testJackson() throws IOException {
		LabeledImage image = new LabeledImage("Carsten", "carsten.tif", "carsten.labeling");
		LabkitProjectModel project = new LabkitProjectModel(SingletonContext.getInstance(), Collections
			.singletonList(image));
		File tmp = File.createTempFile("test", ".yaml");
		tmp.deleteOnExit();
		LabkitProjectSerializer.save(project, tmp);
		String text = FileUtils.readFileToString(tmp, StandardCharsets.UTF_8);
		System.out.println(text);
	}
}
