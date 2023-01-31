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

package sc.fiji.labkit.ui;

import sc.fiji.labkit.ui.actions.LabelEditAction;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.panel.ImageInfoPanel;
import sc.fiji.labkit.ui.panel.LabelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * LabelingComponent provides a UI for displaying an image overlaid with a
 * labeling. It features brush tools for modifying the labeling, and a sidebar
 * that shows a list of all the labels.
 */
public class LabelingComponent extends JPanel implements AutoCloseable {

	private final BasicLabelingComponent labelingComponent;

	public LabelingComponent(JFrame dialogBoxOwner, ImageLabelingModel model) {
		this.labelingComponent = new BasicLabelingComponent(dialogBoxOwner, model);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
		leftPanel.add(ImageInfoPanel.newFramedImageInfoPanel(model, labelingComponent), "grow, wrap");
		DefaultExtensible extensible = new DefaultExtensible(null, dialogBoxOwner);
		new LabelEditAction(extensible, false, new ColoredLabelsModel(model));
		leftPanel.add(LabelPanel.newFramedLabelPanel(model, extensible, false),
			"grow");
		setLayout(new BorderLayout());
		add(initSplitPane(leftPanel, labelingComponent));
	}

	private JSplitPane initSplitPane(JComponent left, JComponent right) {
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(left);
		panel.setRightComponent(right);
		return panel;
	}

	@Deprecated
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void close() {
		labelingComponent.close();
	}
}
