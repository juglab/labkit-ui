
package net.imglib2.labkit.multi_image;

import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.actions.AbstractFileIoAction;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LabkitProjectView extends JPanel {

	private final LabkitProjectModel model;

	private final List<ListDataListener> listeners = new CopyOnWriteArrayList<>();

	private final JList<LabeledImage> list;

	public LabkitProjectView(LabkitProjectModel model, SegmentationComponent component) {
		this.model = model;
		model.changeNotifier().add(this::updateJList);
		setLayout(new MigLayout("", "[grow]", "[grow]0px[]"));
		this.list = initList(model, component);
		list.setModel(new MyListModel());
		add(initScrollPane(list), "grow, wrap");
		JPanel buttonsPanel = initButtonsPanel();
		buttonsPanel.setBackground(list.getBackground());
		add(buttonsPanel, "grow");
	}

	private JScrollPane initScrollPane(JList<LabeledImage> list) {
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	private JPanel initButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new MigLayout());
		JButton addImageButton = new JButton("Add image");
		addImageButton.addActionListener(ignore -> onAddImageClicked(addImageButton));
		buttonsPanel.add(addImageButton);
		JButton removeImageButton = new JButton("Remove image");
		removeImageButton.addActionListener(ignore -> onRemoveImageButtonClicked());
		buttonsPanel.add(removeImageButton);
		return buttonsPanel;
	}

	private void onAddImageClicked(JButton button) {
		JFileChooser chooser = new JFileChooser("Select Image");
		chooser.setFileFilter(AbstractFileIoAction.TIFF_FILTER);
		chooser.setMultiSelectionEnabled(true);
		int result = chooser.showOpenDialog(button);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			for (File file : files) {
				String filename = file.getPath();
				LabeledImage newLabeledImage = new LabeledImage(filename);
				model.labeledImages().add(newLabeledImage);
				model.changeNotifier().notifyListeners();
			}
		}
	}

	private void onRemoveImageButtonClicked() {
		List<LabeledImage> selected = list.getSelectedValuesList();
		for (LabeledImage image : selected)
			model.labeledImages().remove(image);
		model.changeNotifier().notifyListeners();
	}

	private static JList<LabeledImage> initList(LabkitProjectModel labkitProjectModel,
		SegmentationComponent component)
	{
		JList<LabeledImage> list = new JList<>(labkitProjectModel.labeledImages().toArray(
			new LabeledImage[0]));
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				labkitProjectModel.selectLabeledImage(list.getSelectedValue());
				component.autoContrast();
			}
		});
		return list;
	}

	private void updateJList() {
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, model
			.labeledImages().size());
		listeners.forEach(l -> l.contentsChanged(e));
	}

	private class MyListModel implements ListModel<LabeledImage> {

		@Override
		public int getSize() {
			return model.labeledImages().size();
		}

		@Override
		public LabeledImage getElementAt(int index) {
			return model.labeledImages().get(index);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
	}
}
