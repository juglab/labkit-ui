
package sc.fiji.labkit.ui.segmentation.weka;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.ValuePair;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.Collections;

public class TrainableSegmentationSegmenterTest {

	@Test
	public void testClassify3DStackInSlices() {
		Context context = SingletonContext.getInstance();
		TrainableSegmentationSegmenter segmenter = new TrainableSegmentationSegmenter(context);
		// Settings to 2D
		segmenter.setFeatureSettings(new FeatureSettings(GlobalSettings.default2d().build(),
			SingleFeatures.identity()));
		// Train on 3D image
		ImgPlus<?> image3d = new ImgPlus<>(ArrayImgs.ints(new int[] { 0, 1, 1, 0 }, 2, 1, 2), "name",
			new AxisType[] { Axes.X, Axes.Y, Axes.Z });
		Labeling labeling = initLabeling();
		segmenter.train(Collections.singletonList(new ValuePair<>(image3d, labeling)));
		// Segment 3D image
		Img<IntType> result = ArrayImgs.ints(2, 1, 2);
		segmenter.segment(image3d, result);
		ImgLib2Assert.assertImageEquals(image3d, result, Object::equals);
		Img<IntType> expected2d = ArrayImgs.ints(new int[] { 0, 1, 0 }, 1, 3);
		// Segment 2d image
		ImgPlus<IntType> image2d = new ImgPlus<>(expected2d, "");
		Img<IntType> result2d = ArrayImgs.ints(1, 3);
		segmenter.segment(image2d, result2d);
		ImgLib2Assert.assertImageEquals(image2d, result2d, Object::equals);
	}

	private Labeling initLabeling() {
		Labeling labeling = Labeling.createEmpty(Arrays.asList("a", "b"), new FinalInterval(2, 1, 2));
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0, 0 });
		ra.get().add(labeling.getLabel("a"));
		ra.setPosition(new long[] { 1, 0, 0 });
		ra.get().add(labeling.getLabel("b"));
		return labeling;
	}
}
