
package net.imglib2.labkit.v2.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level model in Labkit.
 */
public class LabkitModel {

	private List<SegmenterModel> segmenterModels = new ArrayList<>();

	private List<ImageModel> imageModels = new ArrayList<>();

	// Getters

	public List<SegmenterModel> getSegmenterModels() {
		return segmenterModels;
	}

	public List<ImageModel> getImageModels() {
		return imageModels;
	}
}
