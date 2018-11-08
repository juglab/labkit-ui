
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
		switch (args.length) {
			case 0:
				startWithFileDialog();
				break;
			case 1:
				startWithFilename(args[0]);
				break;
			default:
				exitWithErrorMessage();
				break;
		}
	}

	private static void startWithFileDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);

		FileFilter fileFilter = AbstractFileIoAction.TIFF_FILTER;
		fileChooser.setFileFilter(fileFilter);
		fileChooser.addChoosableFileFilter(fileFilter);

		final int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			System.exit(1);
			return;
		}

		String filename = fileChooser.getSelectedFile().getAbsolutePath();
		startWithFilename(filename);
	}

	private static void startWithFilename(String filename) {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		MainFrame.open(imageJ.context(), filename);
	}

	private static void exitWithErrorMessage() {
		System.err.println("error: only 1 image is supported");
		System.exit(2);
	}
}
