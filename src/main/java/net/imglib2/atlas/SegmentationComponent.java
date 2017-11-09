package net.imglib2.atlas;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SegmentationComponent {

	private final JPanel panel = initPanel();

	public SegmentationComponent(JFrame dialogBoxOwner, RandomAccessibleInterval<? extends NumericType<?>> image) {
		Labeling labeling = new Labeling(Arrays.asList("background", "foreground"), image);
		LabelingComponent labeler = new LabelingComponent(dialogBoxOwner, image, labeling, false);
		panel.add(labeler.getComponent());
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private static JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setSize(100, 100);
		panel.setLayout(new BorderLayout());
		return panel;
	}
}
