
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.InitialLabeling;
import net.imglib2.labkit.project.LabkitProjectFileFilter;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.models.LabkitModelSerialization;
import net.imglib2.labkit.v2.utils.InputImageIoUtils;
import net.imglib2.labkit.v2.views.LabkitView;
import net.imglib2.labkit.v2.views.LabkitViewListener;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;

public class LabkitController implements LabkitViewListener {

	private LabkitModel model;

	private LabkitView view;

	private Context context = SingletonContext.getInstance();

	public LabkitController(LabkitModel model, LabkitView view) {
		this.model = model;
		this.view = view;
		view.setModel(model);
		view.updateImageList();
		view.addListerner(this);
	}

	public void showView() {
		view.setVisible(true);
	}

	// -- Private Helper Methods ---

	@Override
	public void onOpenProject() {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new LabkitProjectFileFilter());
		int result = dialog.showOpenDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		LabkitModel newModel = LabkitModelSerialization.open(file);
		setModel(newModel);
	}

	private void setModel(LabkitModel newModel) {
		model = newModel;
		view.setModel(newModel);
		view.updateImageList();
		view.updateActiveImage();
	}

	@Override
	public void onSaveProject() {
		JFileChooser dialog = new JFileChooser();
		dialog.setFileFilter(new LabkitProjectFileFilter());
		int result = dialog.showSaveDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		LabkitModelSerialization.save(model, file);
	}

	@Override
	public void onChangeActiveImage(ImageModel activeImageModel) {
		model.setActiveImageModel(activeImageModel);
		loadImageModel(activeImageModel);
		view.updateActiveImage();
	}

	@Override
	public void onAddImage() {
		JFileChooser dialog = new JFileChooser();
		int result = dialog.showOpenDialog(view);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		String file = dialog.getSelectedFile().getAbsolutePath();
		ImageModel image = ImageModel.createForImageFile(file);
		model.getImageModels().add(image);
		view.updateImageList();
	}

	private void loadImageModel(ImageModel activeImageModel) {
		String imageFile = activeImageModel.getImageFile();
		if (activeImageModel.getImage() == null)
			activeImageModel.setImage(InputImageIoUtils.open(context, imageFile));
		if (activeImageModel.getLabeling() == null)
			activeImageModel.setLabeling(InitialLabeling.initialLabeling(context, activeImageModel
				.getImage(), activeImageModel.getLabelingFile()));
	}
}
