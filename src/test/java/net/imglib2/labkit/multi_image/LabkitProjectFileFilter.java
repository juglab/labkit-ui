
package net.imglib2.labkit.multi_image;

import java.io.File;

public class LabkitProjectFileFilter extends javax.swing.filechooser.FileFilter {

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().equals("labkit-project.yaml");
	}

	@Override
	public String getDescription() {
		return "Project (labkit-project.yaml)";
	}
}
