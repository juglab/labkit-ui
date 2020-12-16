
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LabkitController {

	private LabkitModel model;

	private LabkitView view;

	public LabkitController(LabkitModel model, LabkitView view) {
		this.model = model;
		this.view = view;
		registerActions();
		registerView();
		listAdapter.triggerUpdate();
	}

	public void showView() {
		view.setVisible(true);
	}

	// -- Private Helper Methods ---

	private void registerActions() {
		view.getAddImageButton().addActionListener(e -> onAddImageClicked());
		view.getImageList().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
				onUserSelectsImage(view.getImageList().getSelectedIndex());
		});
	}

	private void registerView() {
		view.getImageList().setModel(listAdapter);
	}

	private void onAddImageClicked() {
		JFileChooser dialog = new JFileChooser();
		int result = dialog.showOpenDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		ImageModel image = ImageModel.createForImageFile(file);
		model.getImageModels().add(image);
		listAdapter.triggerUpdate();
	}

	private void onUserSelectsImage(int selectedIndex) {
		model.setActiveImageModel(model.getImageModels().get(selectedIndex));
		updateImageView();
	}

	private void updateImageView() {
		ImageModel activeImage = model.getActiveImageModel();
		view.getActiveImageLabel().setText(activeImage.getName());
	}

	private final MyListModel listAdapter = new MyListModel();

	private class MyListModel implements ListModel<String> {

		private final List<ListDataListener> listeners = new CopyOnWriteArrayList<>();

		private void triggerUpdate() {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
			listeners.forEach(l -> l.contentsChanged(e));
		}

		@Override
		public int getSize() {
			return LabkitController.this.model.getImageModels().size();
		}

		@Override
		public String getElementAt(int index) {
			return LabkitController.this.model.getImageModels().get(index).getName();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
	};

}
