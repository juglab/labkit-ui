
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
