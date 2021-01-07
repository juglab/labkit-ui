
package net.imglib2.labkit.v2.controller;

import net.imglib2.labkit.InitialLabeling;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.utils.InputImageIoUtils;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;

import java.io.IOException;

public class ImageController {

	private Context context = SingletonContext.getInstance();

	private ImageModel model;

	public void setModel(ImageModel model) {
		this.model = model;
	}

	// Move this into a ImageModelController

	public void load(String projectFolder) {
		loadImage();
		loadLabeling(projectFolder);
	}

	private void loadImage() {
		if (model.getImage() == null)
			model.setImage(InputImageIoUtils.open(context, model.getImageFile()));
	}

	private void loadLabeling(String projectFolder) {
		if (model.getLabeling() == null) {
			Labeling labeling = InitialLabeling
				.initialLabeling(context, model.getImage(),
					getFullLabelingFile(projectFolder));
			model.setLabeling(labeling);
		}
	}

	private String getFullLabelingFile(String projectFolder) {
		return FilenameUtils.concat(projectFolder, model.getLabelingFile());
	}

	public void saveChanges(String projectFolder) {
		if (model.isLabelingModified()) {
			writeLabeling(projectFolder);
			model.setLabelingModified(false);
		}
	}

	public void writeLabeling(String projectFolder) {
		try {
			new LabelingSerializer(context).save(model.getLabeling(),
				getFullLabelingFile(projectFolder));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
