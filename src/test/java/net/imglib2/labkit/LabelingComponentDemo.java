
package net.imglib2.labkit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class LabelingComponentDemo {

	public static void main(String... args) {
		JFrame frame = initFrame();
		frame.add(initLabelingComponent(frame));
		frame.setVisible(true);
	}

	private static JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private static JComponent initLabelingComponent(JFrame frame) {
		ImageLabelingModel model = initModel();
		LabelingComponent labelingComponent = new LabelingComponent(frame, model);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
		return labelingComponent.getComponent();
	}

	private static ImageLabelingModel initModel() {
		// TODO simplify the creation of an ImageLabelingModel
		RandomAccessibleInterval<? extends NumericType<?>> image = ArrayImgs.bytes(
			100, 100);
		Labeling labeling = new Labeling(Arrays.asList("fg", "bg"), image);
		boolean isTimeSeries = false;
		return new ImageLabelingModel(image, labeling, isTimeSeries);
	}
}
