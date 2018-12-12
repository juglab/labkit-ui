
package net.imglib2.labkit.models;

import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ImageLabelingModelTest {

	@Ignore("broken")
	@Test
	public void testChangeLabelingInterval() {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(8, 8);
		ImageLabelingModel model = new ImageLabelingModel(false);
		model.setImage(image);
		model.labeling().set(initLabeling(4, 4));
		AffineTransform3D labelTransformation = model.labelTransformation();
		assertArrayEquals(expectedTransform(2.0), labelTransformation
			.getRowPackedCopy(), 0.0);
		// process
		model.labeling().set(initLabeling(2, 2));
		assertArrayEquals(expectedTransform(4.0), labelTransformation
			.getRowPackedCopy(), 0.0);
	}

	@Test
	public void testLabelingTransformation() {
		ImageLabelingModel model = new ImageLabelingModel(false);
		final AffineTransform3D transform = transform(1.0, 1.0, 2.0);
		model.showable().set(BdvShowable.wrap(ArrayImgs.ints(10, 10, 10),
			transform));
		model.createEmptyLabeling();
		assertArrayEquals(transform.getRowPackedCopy(), model.labelTransformation()
			.getRowPackedCopy(), 0.0);
	}

	private AffineTransform3D transform(double xScale, double yScale,
		double zScale)
	{
		final AffineTransform3D affineTransform3D = new AffineTransform3D();
		affineTransform3D.set(xScale, 0, 0, 0, 0, yScale, 0, 0, 0, 0, zScale, 0);
		return affineTransform3D;
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
