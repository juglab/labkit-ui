package net.imglib2.atlas;

import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import hr.irb.fastRandomForest.FastRandomForest;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.atlas.actions.BatchSegmentAction;
import net.imglib2.atlas.actions.ChangeFeatureSettingsAction;
import net.imglib2.atlas.actions.ClassifierIoAction;
import net.imglib2.atlas.actions.LabelingIoAction;
import net.imglib2.atlas.actions.OpenImageAction;
import net.imglib2.atlas.actions.OrthogonalView;
import net.imglib2.atlas.actions.SegmentationSave;
import net.imglib2.atlas.actions.SelectClassifier;
import net.imglib2.atlas.actions.SetLabelsAction;
import net.imglib2.atlas.actions.ZAxisScaling;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.atlas.inputimage.DatasetInputImage;
import net.imglib2.atlas.inputimage.InputImage;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.labeling.LabelingSerializer;
import net.imglib2.atlas.plugin.MeasureConnectedComponents;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.Context;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
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

	public static MainFrame open(String filename)
	{
		return open(null, filename);
	}

	public static MainFrame open(Context context, String filename) {
		final Context context2 = (context == null) ? new Context() : context;
		Dataset dataset = RevampUtils.wrapException( () -> context2.service(DatasetIOService.class).open(filename) );
		return new MainFrame(context2, dataset);
	}

	public MainFrame(final Context context, final Dataset dataset)
	{
		this.context = context;
		this.preferences = new Preferences(context);
		this.inputImage = new DatasetInputImage(dataset);
		boolean isTimeSeries = dataset.numDimensions() > 2;
		if(dataset.numDimensions() == 3) {
			int result = JOptionPane.showConfirmDialog(null,
					"Is the given data a time series",
					"Labkit",
					JOptionPane.YES_NO_OPTION);
			isTimeSeries = result == JOptionPane.YES_OPTION;
		}
		inputImage.setTimeSeries(isTimeSeries);
		this.segmentationComponent = new SegmentationComponent(context, frame, inputImage);
		segmentationComponent.setLabeling(getInitialLabeling());
		// --
		SetLabelsAction setlabels = new SetLabelsAction(segmentationComponent::getLabeling,
				segmentationComponent::setLabeling, preferences);
		setTitle();
		MenuBar menubar = new MenuBar(segmentationComponent.getActions());
		menubar.add(setlabels.getMenu());
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
