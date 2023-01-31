/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
