
package net.imglib2.labkit.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatasetInputImageTest {

	DatasetInputImage inputImage = inputImage5d();

	@Test
	public void testInterval() {
		Interval result = inputImage.interval();
		assertTrue(Intervals.equals(new FinalInterval(4, 5, 2, 6), result));
	}

	@Test
	public void testImageForSegmentation() {
		RandomAccessibleInterval<?> result = inputImage.imageForSegmentation();
		assertTrue(Util.getTypeFromInterval(result) instanceof RealType);
		assertTrue(Intervals.equals(new FinalInterval(4, 5, 2, 4, 6), result));
	}

	@Test
	public void testAxes() {
		List<CalibratedAxis> result = inputImage.axes();
		assertEquals(Axes.X, result.get(0).type());
		assertEquals(Axes.Y, result.get(1).type());
		assertEquals(Axes.Z, result.get(2).type());
		assertEquals(Axes.TIME, result.get(3).type());
		assertEquals(4, result.size());
	}

	@Test
	public void testIsTimeSeries() {
		assertTrue(inputImage.isTimeSeries());
		DatasetInputImage input2d = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2)));
		assertFalse(input2d.isTimeSeries());
	}

	@Test
	public void testIsMultiChannel() {
		assertTrue(inputImage.isMultiChannel());
		DatasetInputImage input2d = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2)));
		assertFalse(input2d.isMultiChannel());
		DatasetInputImage color = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2, 3), "", new AxisType[] { Axes.X, Axes.Y,
				Axes.CHANNEL }));
		assertFalse(color.isMultiChannel());
	}

	@Test
	public void testChannelSettingSingle() {
		InputImage single = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2)));
		assertEquals(ChannelSetting.SINGLE, single.getChannelSetting());
	}

	@Test
	public void testChannelSettingColor() {
		InputImage color = new DatasetInputImage(new ImgPlus<>(ArrayImgs.argbs(2,
			2)));
		assertEquals(ChannelSetting.RGB, color.getChannelSetting());
	}

	@Test
	public void testChannelSettingColor2() {
		InputImage color2 = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2, 3), "", new AxisType[] { Axes.X, Axes.Y,
				Axes.CHANNEL }));
		assertEquals(ARGBType.class, color2.imageForSegmentation().randomAccess()
			.get().getClass());
		assertEquals(ChannelSetting.RGB, color2.getChannelSetting());
	}

	@Test
	public void testChannelSettingMultiple() {
		InputImage multi = new DatasetInputImage(new ImgPlus<>(ArrayImgs
			.unsignedBytes(2, 2, 4), "", new AxisType[] { Axes.X, Axes.Y,
				Axes.CHANNEL }));
		assertEquals(ChannelSetting.multiple(4), multi.getChannelSetting());
	}

	private static DatasetInputImage inputImage5d() {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(4, 5, 2, 4, 6);
		ImgPlus<UnsignedByteType> imgPlus = new ImgPlus<>(image, "title",
			new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME });
		return new DatasetInputImage(imgPlus, BdvShowable.wrap(Views.hyperSlice(
			image, 3, 0)));
	}

}
