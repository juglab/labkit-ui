
package net.imglib2.labkit.models;

import org.scijava.Context;

public interface SegmentationModel {

	Context context();

	ImageLabelingModel imageLabelingModel();

	SegmenterListModel segmenterList();
}
