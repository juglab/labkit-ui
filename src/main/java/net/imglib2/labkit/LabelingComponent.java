
package net.imglib2.labkit;

import net.imglib2.labkit.actions.LabelEditAction;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.panel.LabelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LabelingComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final BasicLabelingComponent labelingComponent;

	public LabelingComponent(JFrame dialogBoxOwner, ImageLabelingModel model) {
		this.labelingComponent = new BasicLabelingComponent(dialogBoxOwner, model);
		JPanel leftPanel = new JPanel();
		ActionMap actions = labelingComponent.getActions();
		leftPanel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
		leftPanel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Image"),
			GuiUtils.createDimensionsInfo(model.labeling().get())), "grow, wrap");
		DefaultExtensible extensible =
				new DefaultExtensible(null, null, labelingComponent);
		new LabelEditAction(extensible, false, new ColoredLabelsModel(model));
		LabelPanel labelPanel = new LabelPanel(dialogBoxOwner,
			new ColoredLabelsModel(model), false,
				item -> extensible.createMenu(Label.LABEL_MENU, item));
		leftPanel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Labeling"),
			labelPanel.getComponent()), "grow");
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
