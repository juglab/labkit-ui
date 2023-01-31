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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * {@link LabkitProjectFileSerializer} allows to save and open a
 * {@link LabkitProjectModel}.
 */
public class LabkitProjectFileSerializer {

	private LabkitProjectFileSerializer() {
		// prevent from initialization
	}

	public static void save(LabkitProjectModel project, File file) throws IOException {
		ObjectMapper jackson = new ObjectMapper(new YAMLFactory());
		PlainProjectData plain = asPlainProjectData(project);
		jackson.writeValue(file, plain);
	}

	public static LabkitProjectModel open(Context context, File file) throws IOException {
		ObjectMapper jackson = new ObjectMapper(new YAMLFactory());
		PlainProjectData p = jackson.readValue(file, PlainProjectData.class);
		return asLabkitProjectModel(context, p, file);
	}

	// -- Helper methods for converting LabkitProjectModel to PlainProjectData --

	private static PlainProjectData asPlainProjectData(LabkitProjectModel project) {
		PlainProjectData p = new PlainProjectData();
		p.images = map(x -> asPlainLabeledImage(x), project.labeledImages());
		p.segmentation_algorithms = map(x -> asPlainSegmenter(x), project.segmenterFiles());
		return p;
	}

	private static PlainLabeledImage asPlainLabeledImage(LabeledImage labeledImage) {
		PlainLabeledImage e = new PlainLabeledImage();
		e.nick_name = labeledImage.getName();
		e.image_file = labeledImage.getImageFile();
		e.labeling_file = labeledImage.getLabelingFile();
		return e;
	}

	private static PlainSegmenter asPlainSegmenter(String file) {
		PlainSegmenter s = new PlainSegmenter();
		s.file = file;
		return s;
	}

	public static <T, R> List<R> map(Function<T, R> function, List<T> list) {
		List<R> result = new ArrayList<>(list.size());
		for (T t : list)
			result.add(function.apply(t));
		return result;
	}

	// -- Helper methods for converting PlainProjectData to LabkitProjectModel --

	private static LabkitProjectModel asLabkitProjectModel(Context context, PlainProjectData p,
		File projectFile)
	{
		List<LabeledImage> labeledImageFiles = map(x -> asLabeledImage(context, x), p.images);
		List<String> segmenterFiles = map(x -> x.file, p.segmentation_algorithms);
		LabkitProjectModel project = new LabkitProjectModel(context, projectFile.getParent(),
			labeledImageFiles);
		project.segmenterFiles().addAll(segmenterFiles);
		return project;
	}

	private static LabeledImage asLabeledImage(Context context, PlainLabeledImage image) {
		return new LabeledImage(context, image.nick_name, image.image_file, image.labeling_file);
	}

	// -- Helper classes --

	private static class PlainProjectData {

		public List<PlainLabeledImage> images;

		public List<PlainSegmenter> segmentation_algorithms;
	}

	private static class PlainLabeledImage {

		public String nick_name;

		public String image_file;

		public String labeling_file;
	}

	private static class PlainSegmenter {

		public String file;
	}
}
