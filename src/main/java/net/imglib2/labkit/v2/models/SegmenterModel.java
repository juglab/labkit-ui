
package net.imglib2.labkit.v2.models;

import net.imglib2.labkit.segmentation.Segmenter;

import java.util.Map;

/**
 * Represents a segmentation algorithm.
 */
public class SegmenterModel {

	private String name;

	private String segmenterFile; // could be moved to segmenter

	private Segmenter segmenter;

	private Map<LabeledImageModel, SegmentationModel> results;
}
