
package net.imglib2.labkit.models;

import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class ImageLabelingModelTest {

	@Test
	public void testChangeLabelingInterval() {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(8, 8);
		ImageLabelingModel model = new ImageLabelingModel(new DatasetInputImage(image));
		model.labeling().set(initLabeling(4, 4));
		AffineTransform3D labelTransformation = model.labelTransformation();
		assertArrayEquals(expectedTransform(2.0), labelTransformation
			.getRowPackedCopy(), 0.0);
		// process
		model.labeling().set(initLabeling(2, 2));
		assertArrayEquals(expectedTransform(4.0), labelTransformation
			.getRowPackedCopy(), 0.0);
	}

	private Labeling initLabeling(long... dimensions) {
		return Labeling.createEmpty(Arrays.asList("b", "f"), new FinalInterval(
			dimensions));
	}

	private double[] expectedTransform(double scale) {
		return new double[] { scale, 0.0, 0.0, 0.0, 0.0, scale, 0.0, 0.0, 0.0, 0.0,
			1.0, 0.0 };
	}
}
