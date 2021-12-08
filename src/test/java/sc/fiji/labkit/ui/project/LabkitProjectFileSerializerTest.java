
package sc.fiji.labkit.ui.project;

import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class LabkitProjectFileSerializerTest {

	@Test
	public void testSave() throws IOException {
		LabkitProjectModel project = exampleProject();
		File file = createTmpFile();
		LabkitProjectFileSerializer.save(project, file);
		String text = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		String expected = exampleYaml();
		assertEquals(expected, text);
	}

	@Test
	public void testOpen() throws IOException {
		File file = createTmpFile();
		FileUtils.writeStringToFile(file, exampleYaml(), StandardCharsets.UTF_8);
		LabkitProjectModel project = LabkitProjectFileSerializer.open(SingletonContext.getInstance(),
			file);
		LabkitProjectModel expected = exampleProject();
		assertEquals(expected.labeledImages(), project.labeledImages());
		assertEquals(expected.segmenterFiles(), project.segmenterFiles());
	}

	private File createTmpFile() throws IOException {
		File tmp = File.createTempFile("test", ".yaml");
		tmp.deleteOnExit();
		return tmp;
	}

	private String exampleYaml() {
		String expected = "---\n";
		expected += "images:\n";
		expected += "- nick_name: \"healthy cells\"\n";
		expected += "  image_file: \"cells.tif\"\n";
		expected += "  labeling_file: \"cells.labeling\"\n";
		expected += "segmentation_algorithms:\n";
		expected += "- file: \"/Hello/World.classifier\"\n";
		return expected;
	}

	private LabkitProjectModel exampleProject() {
		LabeledImage image = new LabeledImage(SingletonContext.getInstance(), "healthy cells",
			"cells.tif", "cells.labeling");
		LabkitProjectModel project = new LabkitProjectModel(SingletonContext.getInstance(),
			"/some/path/", Collections
				.singletonList(image));
		project.segmenterFiles().add("/Hello/World.classifier");
		return project;
	}

}
