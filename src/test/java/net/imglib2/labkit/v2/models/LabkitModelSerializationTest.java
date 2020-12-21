
package net.imglib2.labkit.v2.models;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link LabkitModelSerialization}.
 */
public class LabkitModelSerializationTest {

	@Test
	public void testEmptyModel() {
		String inputYaml = emptyYaml();
		LabkitModel model = openModelFromYaml(inputYaml);
		String outputYaml = saveModelToYaml(model);
		assertEquals(inputYaml, outputYaml);
	}

	@Test
	public void testExampleModel() {
		String inputYaml = exampleYaml();
		LabkitModel model = openModelFromYaml(inputYaml);
		String outputYaml = saveModelToYaml(model);
		assertEquals(inputYaml, outputYaml);
	}

	private String saveModelToYaml(LabkitModel model) {
		try {
			File tmp = File.createTempFile("tmp", ".yaml");
			tmp.deleteOnExit();
			LabkitModelSerialization.save(model, tmp.toString());
			return FileUtils.readFileToString(new File(tmp.toString()), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private LabkitModel openModelFromYaml(String yaml) {
		try {
			File tmp = File.createTempFile("tmp", ".yaml");
			tmp.deleteOnExit();
			FileUtils.writeStringToFile(tmp, yaml, StandardCharsets.UTF_8);
			return LabkitModelSerialization.open(tmp.toString());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String emptyYaml() {
		String expected = "---\n";
		expected += "images: []\n";
		expected += "segmentation_algorithms: []\n";
		return expected;
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
}
