package net.imglib2.labkit;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import io.scif.services.DatasetIOService;
import mpicbg.spim.data.SpimDataException;
import net.imagej.Dataset;
import net.imglib2.Interval;
import net.imglib2.labkit.actions.SetLabelsAction;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.util.Intervals;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	public static MainFrame open(Context context, String filename, boolean isTimeSeries) {
		final Context context2 = (context == null) ? new Context() : context;
		Dataset dataset = RevampUtils.wrapException( () -> context2.service(DatasetIOService.class).open(filename) );
		DatasetInputImage inputImage = new DatasetInputImage(dataset);
		inputImage.setTimeSeries(isTimeSeries);
		return new MainFrame(context2, inputImage);
	}

	public static void openXml(Context context, String filename )
	{
		final SpimDataMinimal spimData = RevampUtils.wrapException( () -> new XmlIoSpimDataMinimal().load( filename ) );
		new MainFrame( context, new SpimDataInputImage( spimData, filename ) );
	}

	public MainFrame(final Context context, final InputImage inputImage)
	{
		Preferences preferences = new Preferences( context );
		Labeling initialLabeling = getInitialLabeling(inputImage, context, preferences );
		SegmentationComponent segmentationComponent = initSegmentationComponent( context, inputImage, initialLabeling );
		new SetLabelsAction( segmentationComponent, preferences );
		setTitle( inputImage.getName() );
		frame.setJMenuBar( new MenuBar( segmentationComponent.getActions()) );
		frame.setVisible(true);
	}

	private SegmentationComponent initSegmentationComponent( Context context, InputImage inputImage, Labeling initialLabeling )
	{
		SegmentationComponent segmentationComponent = new SegmentationComponent(context, frame, inputImage, initialLabeling, false);
		frame.add(segmentationComponent.getComponent());
		frame.addWindowListener( new WindowAdapter()
		{
			@Override public void windowClosed( WindowEvent e )
			{
				segmentationComponent.close();
			}
		} );
		return segmentationComponent;
	}

	private Labeling getInitialLabeling(InputImage inputImage, Context context, Preferences preferences) {
		List<String> defaultLabels = preferences.getDefaultLabels();
		return InitialLabeling.initLabeling(inputImage, context, defaultLabels);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setBounds( 50, 50, 1200, 900 );
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private void setTitle( String name ) {
		if(name == null || name.isEmpty())
			frame.setTitle("Labkit");
		else frame.setTitle("Labkit - " + name);
	}
}
