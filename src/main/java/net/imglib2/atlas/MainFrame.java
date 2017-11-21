package net.imglib2.atlas;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.atlas.actions.SetLabelsAction;
import net.imglib2.atlas.inputimage.DatasetInputImage;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;
import net.imglib2.trainable_segmention.RevampUtils;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private final DatasetInputImage inputImage;

	private JFrame frame = initFrame();

	private final Preferences preferences;

	private final Context context;

	private final SegmentationComponent segmentationComponent;

	public static MainFrame open(Context context, String filename, boolean isTimeSeries) {
		final Context context2 = (context == null) ? new Context() : context;
		Dataset dataset = RevampUtils.wrapException( () -> context2.service(DatasetIOService.class).open(filename) );
		return new MainFrame(context2, dataset, isTimeSeries);
	}

	public MainFrame(final Context context, final Dataset dataset, final boolean isTimeSeries)
	{
		this.context = context;
		this.preferences = new Preferences(context);
		this.inputImage = new DatasetInputImage(dataset);
		inputImage.setTimeSeries(isTimeSeries);
		this.segmentationComponent = new SegmentationComponent(context, frame, inputImage);
		segmentationComponent.setLabeling(getInitialLabeling());
		// --
		new SetLabelsAction(segmentationComponent, preferences);
		setTitle();
		MenuBar menubar = new MenuBar(segmentationComponent.getActions());
		frame.setJMenuBar(menubar);
		frame.add(segmentationComponent.getComponent());
		frame.setVisible(true);
	}

	private Labeling getInitialLabeling() {
		List<String> classLabels = preferences.getDefaultLabels();
		String filename = inputImage.getFilename();
		if(new File(filename + ".labeling").exists()) {
			try {
				Labeling labeling = new LabelingSerializer(context).open(filename + ".labeling");
				return labeling;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Labeling labeling = new Labeling(classLabels, inputImage.displayImage());
		labeling.setAxes(inputImage.axes());
		return labeling;
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void setTitle() {
		String name = inputImage.getName();
		if(name == null || name.isEmpty())
			frame.setTitle("Labkit");
		else frame.setTitle("Labkit - " + name);
	}

}
