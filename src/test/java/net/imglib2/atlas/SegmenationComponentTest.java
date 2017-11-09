package net.imglib2.atlas;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;

public class SegmenationComponentTest {

	public static void main(String... args) {
		JFrame frame = setupFrame();
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus("/home/arzt/Documents/Datasets/beans.tif"));
		SegmentationComponent segmenter = new SegmentationComponent(frame, image);
		frame.add(segmenter.getComponent());
		frame.setVisible(true);
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}
