
package net.imglib2.labkit.v2.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level model in Labkit.
 */
public class LabkitModel {

	private List<ImageModel> imageModels = new ArrayList<>();

	private List<SegmenterModel> segmenterModels = new ArrayList<>();

	private ImageModel activeImageModel;

	// Getters

	public List<SegmenterModel> getSegmenterModels() {
		return segmenterModels;
	}

	public List<ImageModel> getImageModels() {
		return imageModels;
	}

	public ImageModel getActiveImageModel() {
		return activeImageModel;
	}

	public void setActiveImageModel(ImageModel activeImageModel) {
		this.activeImageModel = activeImageModel;
	}
}
