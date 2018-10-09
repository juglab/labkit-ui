package net.imglib2.labkit.panel;

import net.imglib2.Dimensions;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.util.Intervals;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ImageInfoPanel {

	private ImageInfoPanel() {
		// prevent from instantiation
	}

	public static JPanel newFramedImageInfoPanel(
			ImageLabelingModel imageLabelingModel)
	{
		return GuiUtils.createCheckboxGroupedPanel(
					imageLabelingModel.imageVisibility(), "Image",
					createDimensionsInfo(
							imageLabelingModel.labeling().get()));
	}

	private static JComponent createDimensionsInfo(Dimensions interval) {
		Color background = UIManager.getColor("List.background");
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 8, gap 0", "10[grow]", ""));
		panel.setBackground(background);
		JLabel label = new JLabel("Dimensions: " + Arrays.toString(Intervals
			.dimensionsAsLongArray(interval)));
		label.setBackground(background);
		label.setOpaque(true);
		panel.add(label, "grow, span");
		return panel;
	}
}
