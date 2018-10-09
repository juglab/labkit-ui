
package net.imglib2.labkit;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

public class SegmentationComponentDemo {

	private final SegmentationComponent segmenter;
	private final DefaultSegmentationModel segmentationModel;

	public static void main(String... args) {
		new SegmentationComponentDemo();
	}

	private SegmentationComponentDemo() {
		JFrame frame = setupFrame();
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"/home/arzt/Documents/Datasets/beans.tif"));
		Context context = new Context();
		segmentationModel = new DefaultSegmentationModel(new DefaultInputImage(
			image), context);
		segmenter = new SegmentationComponent(context, frame, segmentationModel,
			false);
		frame.add(segmenter.getComponent());
		frame.add(getBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
	}

	private JPanel getBottomPanel() {
		JButton segmentation = new JButton(new RunnableAction("Show Segmentation",
			this::showSegmentation));
		JButton prediction = new JButton(new RunnableAction("Show Prediction",
			this::showPrediction));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(segmentation);
		panel.add(prediction);
		return panel;
	}

	private void showSegmentation() {
		if (!segmentationModel.isTrained()) System.out.println("not trained yet");
		else {
			for (RandomAccessibleInterval<UnsignedByteType> segmentation : segmentationModel
				.getSegmentations(new UnsignedByteType()))
			{
				Views.iterable(segmentation).forEach(x -> x.mul(128));
				ImageJFunctions.show(segmentation);
			}
		}
	}

	private void showPrediction() {
		if (!segmentationModel.isTrained()) System.out.println("not trained yet");
		else {
			segmentationModel.getPredictions().forEach(ImageJFunctions::show);
		}
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}
