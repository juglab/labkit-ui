
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.views.LabkitView;

import javax.swing.*;
import java.util.function.Consumer;

public class LabkitController {

	private LabkitModel model;

	private LabkitView view;

	public LabkitController(LabkitModel model, LabkitView view) {
		this.model = model;
		this.view = view;
		registerActions();
	}

	public void showView() {
		view.setVisible(true);
	}

	// -- Private Helper Methods ---

	private void registerActions() {
		view.getAddImageButton().addActionListener(e -> onAddImageClicked());
		view.addImageModelSelectionListener(this::changeSelectedImageModel);
	}

	private void changeSelectedImageModel(ImageModel activeImageModel) {
		model.setActiveImageModel(activeImageModel);
		view.updateActiveImageLabel();
	}

	private void onAddImageClicked() {
		JFileChooser dialog = new JFileChooser();
		int result = dialog.showOpenDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		ImageModel image = ImageModel.createForImageFile(file);
		model.getImageModels().add(image);
		view.updateImageList();
	}

}
