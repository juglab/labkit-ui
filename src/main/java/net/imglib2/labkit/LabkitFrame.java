
package net.imglib2.labkit;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class LabkitFrame {

	private final JFrame frame = initFrame();

	private final Notifier onCloseListeners = new Notifier();

	public static LabkitFrame showForFile(final Context context,
		final String filename)
	{
		final Context context2 = (context == null) ? SingletonContext.getInstance() : context;
		Dataset dataset = CheckedExceptionUtils.run(() -> context2.service(
			DatasetIOService.class).open(filename));
		return showForImage(context2, new DatasetInputImage(dataset));
	}

	public static LabkitFrame showForImage(final Context context,
		final InputImage inputImage)
	{
		final DefaultSegmentationModel model = new DefaultSegmentationModel(
			inputImage, context);
		model.imageLabelingModel().labeling().set(InitialLabeling.initialLabeling(inputImage));
		return show(model, inputImage.imageForSegmentation().getName());
	}

	public static LabkitFrame show(final DefaultSegmentationModel model,
		final String title)
	{
		return new LabkitFrame(model, title);
	}

	private LabkitFrame(final DefaultSegmentationModel model,
		final String title)
	{
		SegmentationComponent segmentationComponent = initSegmentationComponent(
			model);
		setTitle(title);
		frame.setJMenuBar(new MenuBar(segmentationComponent::createMenu));
		frame.setVisible(true);
	}

	private SegmentationComponent initSegmentationComponent(
		DefaultSegmentationModel segmentationModel)
	{
		SegmentationComponent segmentationComponent = new SegmentationComponent(
			frame, segmentationModel, false);
		frame.add(segmentationComponent.getComponent());
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

	public Notifier onCloseListeners() {
		return onCloseListeners;
	}
}
