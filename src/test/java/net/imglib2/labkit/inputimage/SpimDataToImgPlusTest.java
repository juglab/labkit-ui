
package net.imglib2.labkit.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Cast;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class SpimDataToImgPlusTest {

	@Test
	public void testOpen() {
		String filename = SpimDataToImgPlusTest.class.getResource("/export.xml").getPath();
		ImgPlus<UnsignedShortType> result = Cast.unchecked(SpimDataToImgPlus.open(filename, 0));
		assertEquals(Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME), ImgPlusViewsOld
			.getAxes(result));
		Img<IntType> expected = ArrayImgs.ints(IntStream.range(1, 33).toArray(), 2, 2, 2, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}
}
