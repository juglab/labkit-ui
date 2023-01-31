/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.panel;

import net.imglib2.Dimensions;
import sc.fiji.labkit.ui.BasicLabelingComponent;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import net.imglib2.util.Intervals;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * A panel that shows the image size and provides an "auto contrast" button.
 */
public class ImageInfoPanel {

	private ImageInfoPanel() {
		// prevent from instantiation
	}

	public static JPanel newFramedImageInfoPanel(
		ImageLabelingModel imageLabelingModel,
		BasicLabelingComponent labelingComponent)
	{
		return GuiUtils.createCheckboxGroupedPanel(imageLabelingModel
			.imageVisibility(), "Image", createDimensionsInfo(imageLabelingModel
				.labeling().get(), labelingComponent));
	}

	private static JComponent createDimensionsInfo(Dimensions interval,
		BasicLabelingComponent labelingComponent)
	{
		Color background = UIManager.getColor("List.background");
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 8, gap 8", "10[grow][grow]", ""));
		panel.setBackground(background);
		JLabel label = new JLabel("Dimensions: " + Arrays.toString(Intervals
			.dimensionsAsLongArray(interval)));
		panel.add(label, "grow, span, wrap");
		if (labelingComponent != null) {
			final JButton button = new JButton("auto contrast");
			button.setFocusable(false);
			button.addActionListener(ignore -> labelingComponent.autoContrast());
			panel.add(button, "grow");
			final JButton settingsButton = new JButton("settings");
			settingsButton.setFocusable(false);
			settingsButton.addActionListener(ignore -> labelingComponent.toggleContrastSettings());
			panel.add(settingsButton, "grow, wrap");
		}
		return panel;
	}
}
