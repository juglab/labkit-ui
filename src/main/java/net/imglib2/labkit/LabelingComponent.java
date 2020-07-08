
package net.imglib2.labkit;

import net.imglib2.labkit.actions.LabelEditAction;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;

public class LabelingComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final BasicLabelingComponent labelingComponent;

	public LabelingComponent(JFrame dialogBoxOwner, ImageLabelingModel model) {
		this.labelingComponent = new BasicLabelingComponent(dialogBoxOwner, model);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
		leftPanel.add(ImageInfoPanel.newFramedImageInfoPanel(model,
			labelingComponent), "grow, wrap");
		DefaultExtensible extensible = new DefaultExtensible(null, dialogBoxOwner);
		new LabelEditAction(extensible, false, new ColoredLabelsModel(model));
		leftPanel.add(LabelPanel.newFramedLabelPanel(model, extensible, false),
			"grow");
		this.panel = initSplitPane(leftPanel, labelingComponent.getComponent());
	}

	private JSplitPane initSplitPane(JComponent left, JComponent right) {
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(left);
		panel.setRightComponent(right);
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void close() {
		labelingComponent.close();
	}
}
