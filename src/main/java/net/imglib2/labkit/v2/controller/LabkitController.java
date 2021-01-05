
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.InitialLabeling;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.models.LabkitModelSerialization;
import net.imglib2.labkit.v2.utils.InputImageIoUtils;
import net.imglib2.labkit.v2.views.LabkitView;
import net.imglib2.labkit.v2.views.LabkitViewListener;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;

import java.util.List;

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
	public void openProject(String file) {
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
	public void saveProject(String file) {
		LabkitModelSerialization.save(model, file);
	}

	@Override
	public void changeActiveImage(ImageModel activeImageModel) {
		model.setActiveImageModel(activeImageModel);
		if (activeImageModel != null)
			loadImageModel(activeImageModel);
		view.updateActiveImage();
	}

	@Override
	public void addImage(String file) {
		ImageModel image = new ImageModel();
		String name = FilenameUtils.getName(file);
		image.setName(name);
		image.setImageFile(file);
		image.setLabelingFile(name + ".labeling");
		model.getImageModels().add(image);
		view.updateImageList();
	}

	@Override
	public void removeImages(List<ImageModel> imagesToRemove) {
		List<ImageModel> list = model.getImageModels();
		list.removeAll(imagesToRemove);
		if (!list.contains(model.getActiveImageModel()))
			changeActiveImage(list.isEmpty() ? null : list.get(0));
		view.updateImageList();
	}

	private void loadImageModel(ImageModel activeImageModel) {
		String imageFile = activeImageModel.getImageFile();
		if (activeImageModel.getImage() == null)
			activeImageModel.setImage(InputImageIoUtils.open(context, imageFile));
		if (activeImageModel.getLabeling() == null) {
			String fullLabelingFile = FilenameUtils.concat(model.getProjectFolder(), activeImageModel
				.getLabelingFile());
			Labeling labeling = InitialLabeling.initialLabeling(context, activeImageModel.getImage(),
				fullLabelingFile);
			activeImageModel.setLabeling(labeling);
		}
	}
}
