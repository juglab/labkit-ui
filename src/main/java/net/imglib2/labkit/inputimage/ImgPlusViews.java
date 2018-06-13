
package net.imglib2.labkit.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImgView;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

// TODO: make this avaiblable in imglib2
public class ImgPlusViews {

	static boolean canFuseColor(ImgPlus<? extends RealType<?>> image) {
		int d = image.dimensionIndex(Axes.CHANNEL);
		return d >= 0 && image.dimension(d) == 3;
	}

	static ImgPlus<ARGBType> fuseColor(ImgPlus<? extends RealType<?>> image) {
		int d = image.dimensionIndex(Axes.CHANNEL);
		if (d < 0 || image.dimension(d) != 3) throw new IllegalArgumentException();
		RandomAccessibleInterval<ARGBType> colors = Converters.convert(Views
			.collapse(permuteWithLastDimension(image, d)),
			ImgPlusViews::convertToColor, new ARGBType());
		List<CalibratedAxis> list = IntStream.range(0, image.numDimensions())
			.mapToObj(image::axis).collect(Collectors.toList());
		list.remove(d);
		ImgPlus<ARGBType> imgPlus = new ImgPlus<>(ImgView.wrap(colors,
			new CellImgFactory<>()), image.getName());
		for (int i = 0; i < list.size(); i++)
			imgPlus.setAxis(list.get(i).copy(), i);
		return imgPlus;
	}

	private static RandomAccessibleInterval<? extends RealType<?>>
		permuteWithLastDimension(
			RandomAccessibleInterval<? extends RealType<?>> image, int d)
	{
		return (d == image.numDimensions() - 1) ? image : Views.stack(LongStream
			.rangeClosed(image.min(d), image.max(d)).mapToObj(i -> Views.hyperSlice(
				image, d, i)).collect(Collectors.toList()));
	}

	private static void convertToColor(Composite<? extends RealType<?>> in,
		ARGBType out)
	{
		out.set(ARGBType.rgba(toByte(in.get(0)), toByte(in.get(1)), toByte(in.get(
			2)), 255));
	}

	private static int toByte(RealType<?> realType) {
		return (int) realType.getRealFloat();
	}
}
