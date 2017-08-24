package net.imglib2.atlas.actions;

import ij.ImagePlus;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.atlas.MainFrame;
import net.imglib2.img.display.imagej.ImageJFunctions;

/**
 * @author Matthias Arzt
 */
public class OpenImageAction extends AbstractSaveAndLoadAction {


	public OpenImageAction(MainFrame.Extensible extensible) {
		super(extensible);
		initAction("Open Image", filename -> open(filename, false), "");
		initAction("Open Image (Time Series)", filename -> open(filename, true), "");
	}

	private void open(String filename, boolean isTimeSeries) {
		new MainFrame(RevampUtils.uncheckedCast(ImageJFunctions.wrap(new ImagePlus(filename))), isTimeSeries);
	}
}
