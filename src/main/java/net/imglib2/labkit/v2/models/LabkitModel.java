
package net.imglib2.labkit.v2.models;

import java.util.Collections;
import java.util.List;

/**
 * Top level model in Labkit.
 */
public class LabkitModel {

	private List<SegmenterModel> segmenterModels;

	private List<LabeledImageModel> labeledImageModels;

	// Getters

	public List<SegmenterModel> getSegmenterModels() {
		return segmenterModels;
	}

	public List<LabeledImageModel> getLabeledImageModels() {
		return labeledImageModels;
	}
}
