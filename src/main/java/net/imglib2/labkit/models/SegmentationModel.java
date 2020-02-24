
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;

public interface SegmentationModel {

	ImageLabelingModel imageLabelingModel();

	Labeling labeling();

	ImgPlus<?> image();

	CellGrid grid();

	AffineTransform3D labelTransformation();

	void trainSegmenter();

	Holder<Boolean> segmentationVisibility();
}
