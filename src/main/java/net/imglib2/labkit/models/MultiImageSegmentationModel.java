
package net.imglib2.labkit.models;

import org.scijava.Context;

import java.util.List;

public class MultiImageSegmentationModel {

	private final Context context;

	private final List<ImageLabelingModel> imageLabelingModels;

	private final SegmenterListModel segmenterListModel;

	public MultiImageSegmentationModel(Context context,
		List<ImageLabelingModel> imageLabelingModels)
	{
		this.context = context;
		this.imageLabelingModels = imageLabelingModels;
		this.segmenterListModel = new SegmenterListModel(context, imageLabelingModels);
	}

	public List<ImageLabelingModel> imageLabelingModels() {
		return imageLabelingModels;
	}

	public SegmenterListModel segmenterListModel() {
		return segmenterListModel;
	}

	public Context context() {
		return context;
	}
}
