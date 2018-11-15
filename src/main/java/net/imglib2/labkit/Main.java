
package net.imglib2.labkit;

import net.imagej.ImageJ;
import net.imglib2.labkit.actions.AbstractFileIoAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author Matthias Arzt
 */
public class Main {

	public static void main(String... args) {
		String filename = readCommandline(args);
		if(filename == null)
			filename = showOpenDialog();
		if(filename != null)
			start(filename);
	}

	private static String readCommandline(String[] args) {
		switch (args.length) {
			case 1: return args[0];
			case 0: return null;
			default:
				System.err.println("USAGE:    ./start.sh {optional/path/to/image.tif}");
				System.exit(2);
		}
		throw new AssertionError();
	}

	private static String showOpenDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);

		FileFilter fileFilter = AbstractFileIoAction.TIFF_FILTER;
		fileChooser.setFileFilter(fileFilter);
		fileChooser.addChoosableFileFilter(fileFilter);

		final int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		return fileChooser.getSelectedFile().getAbsolutePath();
	}

	public static void start(String filename) {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		MainFrame.open(imageJ.context(), filename);
	}
}
