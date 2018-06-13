
package net.imglib2.labkit;

import net.imagej.ImageJ;
import net.imglib2.labkit.plugin.LabkitPlugin;

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
