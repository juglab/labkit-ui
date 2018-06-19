
package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.SegmenterListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

public class SegmenterPanel {

	private final SegmenterListModel<?> segmentationModel;

	private final JPanel panel = new JPanel();

	private final JList<Object> list = new JList<>();

	public SegmenterPanel(SegmenterListModel<?> segmentationModel,
		ActionMap actions)
	{
		this.segmentationModel = segmentationModel;
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		panel.add(initList(), "grow, wrap");
		panel.add(initAddButton(), "split 2, grow");
		panel.add(initTrainButton(actions), "grow");
	}

	private JButton initAddButton() {
		JButton button = new JButton("Add Classifier");
		button.addActionListener(a -> {
			segmentationModel.addSegmenter();
			updateList();
		});
		return button;
	}

	private JButton initTrainButton(ActionMap actions) {
		Action action = actions.get("Train Classifier");
		JButton button = new JButton(action);
		button.setFocusable(false);
		return button;
	}

	private void updateList() {
		DefaultListModel<Object> model = new DefaultListModel<>();
		segmentationModel.segmenters().forEach(model::addElement);
		list.setModel(model);
		list.setSelectedIndex(segmentationModel.segmenters().indexOf(
			segmentationModel.selectedSegmenter()));
	}

	private JComponent initList() {
		updateList();
		list.addListSelectionListener(this::userChangedSelection);
		return new JScrollPane(list);
	}

	private void userChangedSelection(ListSelectionEvent listSelectionEvent) {
		Object selectedValue = list.getSelectedValue();
		if (selectedValue != null)
			((SegmenterListModel) segmentationModel).selectedSegmenter().set(selectedValue);
	}

	public JComponent getComponent() {
		return panel;
	}
}
