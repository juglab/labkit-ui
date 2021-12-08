
package sc.fiji.labkit.ui.plugin;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.morphology.Dilation;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class SegmentImageWithLabkitPluginTest {

	@Test
	public void test() throws Exception {
		// setup
		ImageJ imageJ = new ImageJ(SingletonContext.getInstance());
		Dataset image = imageJ.scifio().datasetIO().open(fullPath("/blobs.tif"));
		String blobsModel = fullPath("/blobs.classifier");
		Dataset expectedImage = imageJ.scifio().datasetIO().open(fullPath("/blobs_segmentation.tif"));
		// process
		Dataset output = (Dataset) imageJ.command().run(SegmentImageWithLabkitPlugin.class, true,
			"input", image,
			"segmenter_file", blobsModel,
			"use_gpu", false)
			.get().getOutput("output");
		// test
		ImgLib2Assert.assertImageEqualsRealType(expectedImage, output, 0.0);
	}

	private String fullPath(String name) {
		return SegmentImageWithLabkitPluginTest.class.getResource(
			name).getFile();
	}
}
