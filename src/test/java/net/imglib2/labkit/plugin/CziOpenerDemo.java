
package net.imglib2.labkit.plugin;

import bdv.export.ProgressWriterConsole;
import bdv.util.BdvOptions;
import com.google.common.io.PatternFilenameFilter;
import io.scif.img.ImgIOException;
import loci.formats.FormatException;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;

import java.awt.*;
import java.io.IOException;

public class CziOpenerDemo {

	public static void main(String... args) throws IOException, FormatException,
		IncompatibleTypeException, ImgIOException
	{
		String filename = filenameFromCommandLineOrDialog(args);
		run(filename);
	}

	public static void run(String filename) {
		CziOpener opener = new CziOpener(new ProgressWriterConsole());
		InputImage out = opener.openWithDialog(filename).getA();
		out.showable().show("Image", BdvOptions.options().is2D());
	}

	private static String filenameFromCommandLineOrDialog(String[] args) {
		if (args.length == 1) return args[0];
		else {
			FileDialog dialog = new FileDialog((Frame) null,
				"CZI Demo, please open a CZI image");
			dialog.setFilenameFilter(new PatternFilenameFilter(".*\\.czi"));
			dialog.setVisible(true);
			return dialog.getFiles()[0].getAbsolutePath();
		}
	}
}
