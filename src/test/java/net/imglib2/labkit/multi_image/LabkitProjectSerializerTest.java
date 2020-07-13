
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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LabkitProjectSerializerTest {

	@Test
	public void testSave() throws IOException {
		LabeledImage image = exampleLabeledImage();
		LabkitProjectModel project = new LabkitProjectModel(SingletonContext.getInstance(), Collections
			.singletonList(image));
		File file = createTmpFile();
		LabkitProjectSerializer.save(project, file);
		String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		String expected = exampleYaml();
		assertEquals(expected, text);
	}

	@Test
	public void testOpen() throws IOException {
		File file = createTmpFile();
		FileUtils.writeStringToFile(file, exampleYaml(), StandardCharsets.UTF_8);
		LabkitProjectModel project = LabkitProjectSerializer.open(SingletonContext.getInstance(), file);
		List<LabeledImage> labeledImages = project.labeledImages();
		assertEquals(Collections.singletonList(exampleLabeledImage()), labeledImages);
	}

	private File createTmpFile() throws IOException {
		File tmp = File.createTempFile("test", ".yaml");
		tmp.deleteOnExit();
		return tmp;
	}

	private LabeledImage exampleLabeledImage() {
		return new LabeledImage("healthy cells", "cells.tif", "cells.labeling");
	}

	private String exampleYaml() {
		String expected = "---\n";
		expected += "images:\n";
		expected += "- nick_name: \"healthy cells\"\n";
		expected += "  image_file: \"cells.tif\"\n";
		expected += "  labeling_file: \"cells.labeling\"\n";
		return expected;
	}

}
