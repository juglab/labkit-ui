
package net.imglib2.labkit.v2.models;

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import java.util.List;

public class SegmentationModel {

	private List<String> classNames;

	private List<ARGBType> classColors;

	private ImgPlus<IntegerType<?>> segmentation;

	private ImgPlus<RealType<?>> probabilityMap;
}
