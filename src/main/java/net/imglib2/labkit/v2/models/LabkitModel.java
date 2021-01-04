
package net.imglib2.labkit.v2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level model in Labkit.
 */
public class LabkitModel {

	private String projectFolder;

	@JsonProperty("images")
	private List<ImageModel> imageModels = new ArrayList<>();

	@JsonProperty("segmentation_algorithms")
	private List<SegmenterModel> segmenterModels = new ArrayList<>();

	@JsonIgnore
	private ImageModel activeImageModel;

	public LabkitModel(String projectFolder) {
		this.projectFolder = projectFolder;
	}

	// Getters

	public String getProjectFolder() {
		return projectFolder;
	}

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
