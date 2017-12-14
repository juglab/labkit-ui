package net.imglib2.labkit;

public class PaintLabelsAndTrain
{

	private static final String em = "/home/arzt/Downloads/epfl-em/training.tif";
	private static final String boats = "/home/arzt/Documents/Datasets/boats.tif";
	private static final String beans = "/home/arzt/Documents/Datasets/beans.tif";
	private static final String lung = "/home/arzt/Documents/Datasets/20170804_LungImages/2017_08_03__0004.jpg";
	private static final String cells = "/home/arzt/Documents/Notes/Tr2d/ProjectFiles/raw.tif";

	public static void start(String filename) {
		MainFrame.open(null, filename, false);
	}

	public static void main( final String[] args ) {
		start(em);
	}
}
