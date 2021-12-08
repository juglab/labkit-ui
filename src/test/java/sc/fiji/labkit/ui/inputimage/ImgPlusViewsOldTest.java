
package sc.fiji.labkit.ui.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class ImgPlusViewsOldTest {

	@Test
	public void testSortAxis() {
		ImgPlus<BitType> image = new ImgPlus<>(ArrayImgs.bits(1, 2, 3, 4), "",
			new AxisType[] { Axes.TIME, Axes.CHANNEL, Axes.Y, Axes.unknown() });
		List<AxisType> order = Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL,
			Axes.TIME);
		ImgPlus<BitType> result = ImgPlusViewsOld.sortAxes(image, order);
		assertArrayEquals(new long[] { 4, 3, 2, 1 }, Intervals
			.dimensionsAsLongArray(result));
	}
}
