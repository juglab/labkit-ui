package net.imglib2.labkit;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SegmentationComponentHdf5Demo
{
	public static void main( String... args ) throws SpimDataException
	{
		new SegmentationComponentHdf5Demo();
	}

	private SegmentationComponentHdf5Demo() throws SpimDataException
	{
		final String fn = "/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		MainFrame.openXml(new Context(), fn);
	}
}
