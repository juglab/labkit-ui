package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class SegmentationModel
{

	private final ImageLabelingModel imageLabelingModel;
	private final Segmenter segmenter;

	private final RandomAccessibleInterval< ? > compatibleImage;
	private final CellGrid grid;

	public SegmentationModel( RandomAccessibleInterval< ? > compatibleImage, ImageLabelingModel imageLabelingModel, Segmenter segmenter )
	{
		this.imageLabelingModel = imageLabelingModel;
		this.segmenter = segmenter;
		this.compatibleImage = compatibleImage;
		this.grid = LabkitUtils.suggestGrid( this.compatibleImage, imageLabelingModel.isTimeSeries() );
	}

	public Labeling labeling() {
		return imageLabelingModel.labeling().get();
	}

	public RandomAccessibleInterval< ? > image() {
		return compatibleImage;
	}

	public CellGrid grid() {
		return grid;
	}

	public Segmenter segmenter() {
		return segmenter;
	}

	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}

	public AffineTransform3D labelTransformation()
	{
		return imageLabelingModel.labelTransformation();
	}
}
