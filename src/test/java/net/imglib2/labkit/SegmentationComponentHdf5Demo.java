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
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( fn );
		JFrame frame = setupFrame();
		Context context = new Context();
		SegmentationComponent segmenter = new SegmentationComponent(context, frame, new SpimDataInputImage( spimData ));
		frame.add(segmenter.getComponent());
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosed( WindowEvent e )
			{
				segmenter.close();
			}
		} );
		frame.setVisible(true);
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}
