
package net.imglib2.labkit.multi_image;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LabkitProjectSerializer allows to save an {@link LabkitProjectModel} to file,
 * and to open it from file.
 */
public class LabkitProjectSerializer {

	private LabkitProjectSerializer() {
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
		return asLabkitProjectModel(context, p);
	}

	// -- Helper methods for converting LabkitProjectModel to PlainProjectData --

	private static PlainProjectData asPlainProjectData(LabkitProjectModel project) {
		PlainProjectData p = new PlainProjectData();
		p.images = new ArrayList<>();
		for (LabeledImage labeledImage : project.labeledImages()) {
			p.images.add(asPlainLabeledImage(labeledImage));
		}
		return p;
	}

	private static PlainLabeledImage asPlainLabeledImage(LabeledImage labeledImage) {
		PlainLabeledImage e = new PlainLabeledImage();
		e.nick_name = labeledImage.getName();
		e.image_file = labeledImage.getImageFile();
		e.labeling_file = labeledImage.getLabelingFile();
		return e;
	}

	// -- Helper methods for converting PlainProjectData to LabkitProjectModel --

	private static LabkitProjectModel asLabkitProjectModel(Context context, PlainProjectData p) {
		return new LabkitProjectModel(context, asLabeledImages(p.images));
	}

	private static List<LabeledImage> asLabeledImages(List<PlainLabeledImage> images) {
		ArrayList<LabeledImage> list = new ArrayList<>();
		for (PlainLabeledImage image : images)
			list.add(asLabeledImage(image));
		return list;
	}

	private static LabeledImage asLabeledImage(PlainLabeledImage image) {
		return new LabeledImage(image.nick_name, image.image_file, image.labeling_file);
	}

	// -- Helper classes --

	private static class PlainProjectData {

		public List<PlainLabeledImage> images;
	}

	private static class PlainLabeledImage {

		public String nick_name;

		public String image_file;

		public String labeling_file;
	}

}
