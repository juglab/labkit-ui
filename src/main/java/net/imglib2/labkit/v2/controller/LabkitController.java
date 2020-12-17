
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.utils.InputImageIoUtils;
import net.imglib2.labkit.v2.views.LabkitView;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;

public class LabkitController {

	private LabkitModel model;

	private LabkitView view;

	private Context context = SingletonContext.getInstance();

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
		loadImage(activeImageModel);
		view.updateActiveImage();
	}

	private void loadImage(ImageModel activeImageModel) {
		String imageFile = activeImageModel.getImageFile();
		activeImageModel.setImage(InputImageIoUtils.open(context, imageFile));
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
