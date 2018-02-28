package net.imglib2.labkit;

import mpicbg.spim.data.SpimDataException;
import org.scijava.Context;

public class SegmentationComponentHdf5Demo
{
	public static void main( String... args ) throws SpimDataException
	{
		new SegmentationComponentHdf5Demo();
	}

	private SegmentationComponentHdf5Demo() throws SpimDataException
	{
		final String mouse = "/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		final String xwing = "/home/arzt/Documents/Datasets/XWing/xwing.xml";
		MainFrame.openXml(new Context(), mouse);
	}
}
