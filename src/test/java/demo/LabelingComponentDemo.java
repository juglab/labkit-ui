
package demo;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.img.VirtualStackAdapter;
import sc.fiji.labkit.ui.LabelingComponent;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.models.ImageLabelingModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Demonstrates how the {@link LabelingComponent} can be used.
 */
public class LabelingComponentDemo {

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageLabelingModel model = initModel();
		frame.add(initLabelingComponent(frame, model));
		frame.setVisible(true);
	}

	private static ImageLabelingModel initModel() {
		final ImagePlus imp = new ImagePlus(
			"https://imagej.nih.gov/ij/images/FluorescentCells.jpg");
		ImgPlus<?> image = VirtualStackAdapter.wrap(imp);
		return new ImageLabelingModel(new DatasetInputImage(image));
	}

	private static JComponent initLabelingComponent(JFrame frame,
		ImageLabelingModel model)
	{
		LabelingComponent labelingComponent = new LabelingComponent(frame, model);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
		return labelingComponent;
	}
}
