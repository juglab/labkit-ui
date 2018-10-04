
package net.imglib2.labkit;

import net.imagej.ImageJ;
import net.imglib2.labkit.actions.AbstractFileIoAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class Main {

	public static void main(String... args) {
		fileChooserAndThen(filename -> Main.start(filename));
	}

	static private void fileChooserAndThen(Consumer<String> action) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter fileFilter = AbstractFileIoAction.TIFF_FILTER;
		fileChooser.setFileFilter(fileFilter);
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		final int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) action.accept(fileChooser
			.getSelectedFile().getAbsolutePath());
	}

	public static void start(String filename) {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		MainFrame.open(imageJ.context(), filename);
	}
}
