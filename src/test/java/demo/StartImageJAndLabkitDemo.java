
package demo;

import net.imagej.ImageJ;
import sc.fiji.labkit.ui.plugin.LabkitPlugin;

import java.io.IOException;

public class StartImageJAndLabkitDemo {

	public static void main(String... args) throws IOException {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Object data = imageJ.io().open("https://imagej.nih.gov/ij/images/leaf.jpg");
		imageJ.ui().show(data);
		imageJ.command().run(LabkitPlugin.class, true);
	}
}
