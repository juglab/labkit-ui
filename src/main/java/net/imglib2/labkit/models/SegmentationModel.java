
package net.imglib2.labkit.models;

import org.scijava.Context;

/**
 * Model that holds an image, a labeling and a list of segmentation algorithms.
 */
public interface SegmentationModel {

	Context context();

	ImageLabelingModel imageLabelingModel();

	SegmenterListModel segmenterList();
}
