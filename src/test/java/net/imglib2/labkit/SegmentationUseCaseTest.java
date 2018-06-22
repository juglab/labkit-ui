
package net.imglib2.labkit;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SegmentationUseCaseTest {

	@Test
	public void test() {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(new byte[] { 1, 1, 2,
			2 }, 2, 2);
		InputImage inputImage = new DefaultInputImage(image);
		ImageLabelingModel imageLabelingModel = new ImageLabelingModel(image,
			new Labeling(Arrays.asList("b", "f"), image), false);
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			image, imageLabelingModel, () -> new TrainableSegmentationSegmenter(
				new Context(), inputImage));
		addLabels(imageLabelingModel);
		SegmentationItem segmenter = segmentationModel.segmenters().get(0);
		segmenter.segmenter().train(Collections.singletonList(image), Collections
			.singletonList(imageLabelingModel.labeling().get()));
		RandomAccessibleInterval<ShortType> result = segmenter.results()
			.segmentation();
		List<Integer> list = new ArrayList<>();
		Views.iterable(result).forEach(x -> list.add(x.getInteger()));
		assertEquals(Arrays.asList(1, 1, 0, 0), list);
	}

	private void addLabels(ImageLabelingModel imageLabelingModel) {
		Labeling labeling = imageLabelingModel.labeling().get();
		RandomAccess<Set<String>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add("f");
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add("b");
	}
}
