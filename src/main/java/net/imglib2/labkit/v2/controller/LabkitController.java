
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.LabeledImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

import javax.swing.*;

public class LabkitController {

	private LabkitModel model;

	private LabkitView view;

	public LabkitController(LabkitModel model, LabkitView view) {
		this.model = model;
		this.view = view;
		registerActions();
		updateListView();
	}

	public void showView() {
		view.setVisible(true);
	}

	// -- Private Helper Methods ---

	private void registerActions() {
		view.getAddImageButton().addActionListener(e -> onAddImageClicked());
	}

	private void onAddImageClicked() {
		JFileChooser dialog = new JFileChooser();
		int result = dialog.showOpenDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		LabeledImageModel image = LabeledImageModel.createForImageFile(file);
		model.getLabeledImageModels().add(image);
		updateListView();
	}

	private void updateListView() {
		DefaultListModel<String> imageList = view.getImageList();
		imageList.removeAllElements();
		for (LabeledImageModel imageModel : model.getLabeledImageModels()) {
			imageList.addElement(imageModel.getName());
		}
	}
}
