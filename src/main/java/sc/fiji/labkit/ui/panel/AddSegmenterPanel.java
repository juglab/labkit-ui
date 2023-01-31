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

import sc.fiji.labkit.ui.models.ExtensionPoints;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.SegmentationPluginService;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that shows a list of available segmentation algorithms. This panel is
 * usually shown only if the {@link SegmenterPanel} is empty.
 */
public class AddSegmenterPanel extends JPanel {

	public AddSegmenterPanel(SegmenterListModel segmenterListModel) {
		setLayout(new BorderLayout());
		JPanel list = new JPanel(new MigLayout("", "[grow]"));
		list.setBackground(UIManager.getColor("List.background"));
		list.add(new JLabel("Add segmentation algorithm:"), "wrap");
		addButtons(segmenterListModel, list);
		add(list);
	}

	private void addButtons(SegmenterListModel segmenterListModel, JPanel list) {
		Context context = segmenterListModel.context();
		SegmentationPluginService pluginService = context.service(SegmentationPluginService.class);
		for (SegmentationPlugin sp : pluginService.getSegmentationPlugins()) {
			JButton button = new JButton(sp.getTitle());
			button.addActionListener(ignore -> {
				segmenterListModel.addSegmenter(sp);
			});
			list.add(button, "grow, wrap");
		}
	}

	public static void main(String... args) {
		JFrame frame = new JFrame("Select Segmentation Algorithm");
		Context context = SingletonContext.getInstance();
		SegmenterListModel slm = new SegmenterListModel(context, new ExtensionPoints());
		frame.add(new AddSegmenterPanel(slm));
		frame.setSize(300, 300);
		frame.setVisible(true);
	}
}
