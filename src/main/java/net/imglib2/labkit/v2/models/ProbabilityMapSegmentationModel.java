
package net.imglib2.labkit.v2.models;

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;

// TODO: Allow to represent segmentation results with overlapping labels.
public class ProbabilityMapSegmentationModel extends SegmentationModel {

	private ImgPlus<RealType<?>> probabilityMap;
}
