package net.imglib2.atlas;

import ij.ImagePlus;

import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class PaintLabelsAndTrain
{

	public static void em()
	{

		start(System.getProperty( "user.home" ) + "/Downloads/epfl-em/training.tif");
	}

	public static void boats()
	{
		start(System.getProperty( "user.home" ) + "/Documents/Datasets/boats.tif");
	}

	public static void lung() {
		start("/home/arzt/Documents/Datasets/20170804_LungImages/2017_08_03__0004.jpg");
	}

	public static void start(String imgPath) {
		Img<?> img = ImageJFunctions.wrap(new ImagePlus(imgPath));
		new MainFrame(RevampUtils.uncheckedCast(img), false);
	}

	public static void main( final String[] args ) {
		boats();
	}
}
