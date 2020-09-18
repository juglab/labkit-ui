
package net.imglib2.labkit.project;

import java.io.File;

/**
 * File filter that only matched files named "labkit-project.yaml"
 */
public class LabkitProjectFileFilter extends javax.swing.filechooser.FileFilter {

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().equals("labkit-project.yaml");
	}

	@Override
	public String getDescription() {
		return "Labkit Project (labkit-project.yaml)";
	}
}
