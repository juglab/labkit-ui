
package net.imglib2.labkit.bdv;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ImgPlusBdvShowable implements BdvShowable {

	private final ImgPlus<? extends NumericType<?>> image;
	private AxisOrder axisOrder;

	ImgPlusBdvShowable(ImgPlus<? extends NumericType<?>> image) {
		this.image = image;
	}

	@Override
	public Interval interval() {
		return image;
	}

	@Override
	public AffineTransform3D transformation() {
		return new AffineTransform3D();
	}

	@Override
	public BdvStackSource<?> show(String title, BdvOptions options) {
		String name = image.getName();
		return BdvFunctions.show(image, name == null ? title : name, options.axisOrder(
			getAxisOrder()));
	}

	public AxisOrder getAxisOrder() {
		String code = IntStream.range(0, image.numDimensions()).mapToObj(i -> image
			.axis(i).type().getLabel().substring(0, 1)).collect(Collectors.joining());
		try {
			return AxisOrder.valueOf(code);
		}
		catch (IllegalArgumentException e) {
			return AxisOrder.DEFAULT;
		}
	}
}
