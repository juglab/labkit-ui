
package demo.mats_1;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.BasicLabelingComponent;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This is the window/dialog shown in {@link MatsDemo1}.
 */
public class LabelingDialog extends JDialog {

	public LabelingDialog(ImagePlus image, Labeling labeling) {
		super((Frame) null, "Hello World!");

		// add a label on top
		JLabel label = new JLabel(
			"Please use the pen to mark the background as blue.");
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(24.f));
		add(BorderLayout.PAGE_START, label);

		// add the labeling component to the middle of the window
		BasicLabelingComponent labelingComponent = initLabelingComponent(image,
			labeling);
		add(labelingComponent.getComponent());

		// free the memory when the window is closed
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
	}

	private BasicLabelingComponent initLabelingComponent(ImagePlus image,
		Labeling labeling)
	{
		Img<? extends NumericType<?>> img = ImageJFunctions.wrap(image);
		boolean isTimeSeries = false;
		ImageLabelingModel model = new ImageLabelingModel(img, labeling,
			isTimeSeries);
		BasicLabelingComponent labelingComponent = new BasicLabelingComponent(null,
			model);
		return labelingComponent;
	}

}
