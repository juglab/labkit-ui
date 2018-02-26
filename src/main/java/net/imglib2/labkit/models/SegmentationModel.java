package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class SegmentationModel
{

	private final ImageLabelingModel imageLabelingModel;
	private final Segmenter segmenter;

	private final RandomAccessibleInterval< ? > compatibleImage;
	private final CellGrid grid;

	public SegmentationModel( ImageLabelingModel imageLabelingModel, Segmenter segmenter )
	{
		this.imageLabelingModel = imageLabelingModel;
		this.segmenter = segmenter;
		this.compatibleImage = TrainableSegmentationSegmenter.prepareOriginal( imageLabelingModel.image() );
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

	public double scaling() {
		return imageLabelingModel.scaling();
	}

	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}
}
