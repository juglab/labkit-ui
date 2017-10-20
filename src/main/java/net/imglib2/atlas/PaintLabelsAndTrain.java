package net.imglib2.atlas;

import ij.ImagePlus;

import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class PaintLabelsAndTrain
{

	private static final String em = "/home/arzt/Downloads/epfl-em/training.tif";
	private static final String boats = "/home/arzt/Documents/Datasets/boats.tif";
	private static final String beans = "/home/arzt/Documents/Datasets/beans.tif";
	private static final String lung = "/home/arzt/Documents/Datasets/20170804_LungImages/2017_08_03__0004.jpg";

	public static void start(String imgPath) {
		Img<?> img = ImageJFunctions.wrap(new ImagePlus(imgPath));
		new MainFrame(RevampUtils.uncheckedCast(img), false);
	}

	public static void main( final String[] args ) {
		start(beans);
	}
}
