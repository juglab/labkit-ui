package net.imglib2.labkit;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.labkit.actions.SetLabelsAction;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.util.Intervals;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private final InputImage inputImage;

	private JFrame frame = initFrame();

	private final Preferences preferences;

	private final Context context;

	private final SegmentationComponent segmentationComponent;

	public static MainFrame open(Context context, String filename, boolean isTimeSeries) {
		final Context context2 = (context == null) ? new Context() : context;
		Dataset dataset = RevampUtils.wrapException( () -> context2.service(DatasetIOService.class).open(filename) );
		DatasetInputImage inputImage = new DatasetInputImage(dataset);
		inputImage.setTimeSeries(isTimeSeries);
		return new MainFrame(context2, inputImage);
	}

	public MainFrame(final Context context, final InputImage inputImage)
	{
		this.context = context;
		this.preferences = new Preferences(context);
		this.inputImage = inputImage;
		Labeling initialLabeling = getInitialLabeling();
		inputImage.setScaling(getScaling(inputImage, initialLabeling));
		this.segmentationComponent = initSegmentationComponent( context, inputImage, initialLabeling );
		new SetLabelsAction(segmentationComponent, preferences);
		setTitle();
		frame.setJMenuBar( new MenuBar(segmentationComponent.getActions()) );
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

	private double getScaling(InputImage inputImage, Labeling initialLabeling) {
		long[] dimensionsA = Intervals.dimensionsAsLongArray(inputImage.displayImage());
		long[] dimensionsB = Intervals.dimensionsAsLongArray(initialLabeling);
		return IntStream.range(0, dimensionsA.length).mapToDouble(i -> (double) dimensionsB[i] / (double) dimensionsA[i]).average().orElse(1.0);
	}

	private Labeling getInitialLabeling() {
		List<String> defaultLabels = preferences.getDefaultLabels();
		return InitialLabeling.initLabeling(inputImage, context, defaultLabels);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setBounds( 50, 50, 1200, 900 );
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private void setTitle() {
		String name = inputImage.getName();
		if(name == null || name.isEmpty())
			frame.setTitle("Labkit");
		else frame.setTitle("Labkit - " + name);
	}

}
