
package net.imglib2.labkit.multi_image;

import net.imglib2.labkit.models.LabkitProjectModel;

import java.io.File;
import java.util.List;

public class LabkitProjectSerializer {

	private static class JacksonData {

		public List<JacksonImage> images;

		public JacksonData() {}
	}

	private static class JacksonImage {

		public String nick_name;

		public String image_file;

		public String labeling_file;
	}

	public static void save(LabkitProjectModel project, File tmp) {}
}
