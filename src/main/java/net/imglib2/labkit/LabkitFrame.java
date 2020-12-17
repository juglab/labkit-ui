
package net.imglib2.labkit;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.utils.Listeners;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * The main Labkit window. (This window allows to segment a single image. It has
 * to be distinguished from the LabkitProjectFrame, which allows to operation on
 * multiple images.) The window only contains a {@link SegmentationComponent}
 * and shows the associated main menu.
 *
 * @author Matthias Arzt
 */
public class LabkitFrame {

	private final JFrame frame = initFrame();

	private final Listeners onCloseListeners = new Listeners();

	public static LabkitFrame showForFile(Context context,
		final String filename)
	{
		if (context == null)
			context = SingletonContext.getInstance();
		Dataset dataset = openDataset(context, filename);
		return showForImage(context, new DatasetInputImage(dataset));
	}

	private static Dataset openDataset(Context context, String filename) {
		try {
			return context.service(DatasetIOService.class).open(filename);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static LabkitFrame showForImage(final Context context,
		final InputImage inputImage)
	{
		final SegmentationModel model = new DefaultSegmentationModel(context, inputImage);
		model.imageLabelingModel().labeling().set(InitialLabeling.initialLabeling(context, inputImage,
			inputImage.filename() + "labeling"));
		return show(model, inputImage.imageForSegmentation().getName());
	}

	public static LabkitFrame show(final SegmentationModel model,
		final String title)
	{
		return new LabkitFrame(model, title);
	}

	private LabkitFrame(final SegmentationModel model,
		final String title)
	{
		SegmentationComponent segmentationComponent = initSegmentationComponent(
			model);
		setTitle(title);
		frame.setJMenuBar(new MenuBar(segmentationComponent::createMenu));
		frame.setVisible(true);
	}

	private SegmentationComponent initSegmentationComponent(
		SegmentationModel segmentationModel)
	{
		SegmentationComponent segmentationComponent = new SegmentationComponent(
			frame, segmentationModel, false);
		frame.add(segmentationComponent);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				segmentationComponent.close();
				onCloseListeners.notifyListeners();
			}
		});
		return segmentationComponent;
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setBounds(50, 50, 1200, 900);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private void setTitle(String name) {
		if (name == null || name.isEmpty()) frame.setTitle("Labkit");
		else frame.setTitle("Labkit - " + name);
	}

	public Listeners onCloseListeners() {
		return onCloseListeners;
	}
}
