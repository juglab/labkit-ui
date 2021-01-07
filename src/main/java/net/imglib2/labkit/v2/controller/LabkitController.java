
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.models.LabkitModelSerialization;
import net.imglib2.labkit.v2.views.LabkitView;
import net.imglib2.labkit.v2.views.LabkitViewListener;
import org.apache.commons.io.FilenameUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LabkitController implements LabkitViewListener {

	private LabkitModel model;

	private LabkitView view;

	private ImageController imageController = new ImageController();

	public LabkitController(LabkitModel model, LabkitView view) {
		this.model = model;
		this.view = view;
		view.setModel(model);
		view.updateImageList();
		view.addListener(this);
	}

	public void showView() {
		view.setVisible(true);
	}

	// -- Private Helper Methods ---

	@Override
	public void openProject(String file) {
		LabkitModel newModel = LabkitModelSerialization.open(file);
		newModel.setProjectFolder(FilenameUtils.getFullPath(file));
		setModel(newModel);
	}

	private void setModel(LabkitModel newModel) {
		model = newModel;
		view.setModel(newModel);
		view.updateImageList();
		view.updateActiveImage();
	}

	@Override
	public void saveProject() {
		String yaml = FilenameUtils.concat(model.getProjectFolder(), "labkit-project.yaml");
		LabkitModelSerialization.save(model, yaml);
		saveLabelings(model.getProjectFolder());
	}

	@Override
	public void saveProjectAs(String file) {
		LabkitModelSerialization.save(model, file);
		saveLabelings(FilenameUtils.getFullPath(file));
	}

	private void saveLabelings(String projectFolder) {
		boolean inplace = model.getProjectFolder().equals(projectFolder);
		ImageController ic = new ImageController();
		for (ImageModel imageModel : model.getImageModels()) {
			ic.setModel(imageModel);
			if (inplace)
				ic.saveChanges(projectFolder);
			else
				ic.writeLabeling(projectFolder);
		}
	}

	@Override
	public void changeActiveImage(ImageModel activeImageModel) {
		model.setActiveImageModel(activeImageModel);
		if (activeImageModel != null) {
			imageController.setModel(activeImageModel);
			imageController.load(model.getProjectFolder());
		}
		view.updateActiveImage();
	}

	@Override
	public void addImage(String file) {
		ImageModel image = new ImageModel();
		String name = FilenameUtils.getName(file);
		image.setName(name);
		image.setImageFile(file);
		image.setLabelingFile(newLabelingFileName(name));
		model.getImageModels().add(image);
		view.updateImageList();
	}

	private String newLabelingFileName(String name) {
		Set<String> labelingFiles = model.getImageModels().stream()
			.map(ImageModel::getLabelingFile).collect(Collectors.toSet());
		String labelingFile = name + ".labeling";
		for (int i = 1; labelingFiles.contains(labelingFile); i++) {
			labelingFile = name + "_" + i + ".labeling";
		}
		return labelingFile;
	}

	@Override
	public void removeImages(List<ImageModel> imagesToRemove) {
		List<ImageModel> list = model.getImageModels();
		list.removeAll(imagesToRemove);
		if (!list.contains(model.getActiveImageModel()))
			changeActiveImage(list.isEmpty() ? null : list.get(0));
		view.updateImageList();
	}

}
