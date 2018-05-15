package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.fill.Filter;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.operators.ValueEquals;
import net.imglib2.util.Pair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;

public class FloodFillClick extends LabkitBrush implements ClickBehaviour {

	private final boolean value;

	FloodFillClick(boolean value, ViewerPanel viewer, BitmapModel model) {
		super(viewer, model);
		this.value = value;
	}

	protected void floodFill( final RealLocalizable coords)
	{
		synchronized ( viewer )
		{
			RandomAccessibleInterval<BitType> region = model.bitmap();
			Point seed = roundAndReduceDimension(coords, region.numDimensions());
			floodFill(region, seed, new BitType(value));
		}
	}

	private Point roundAndReduceDimension(final RealLocalizable realLocalizable, int numDimesions) {
		Point point = new Point(numDimesions);
		for (int i = 0; i < point.numDimensions(); i++)
			point.setPosition((long) realLocalizable.getDoublePosition(i), i);
		return point;
	}

	@Override
	public void click(int x, int y) {
		floodFill( displayToImageCoordinates(x, y) );
		fireBitmapChanged();
	}

	public static <T extends Type<T> & ValueEquals<T>> void floodFill(RandomAccessibleInterval<T> image, Localizable seed, T value) {
		Filter<Pair<T, T>, Pair<T, T>> filter = (f, s) -> ! value.valueEquals(f.getB());
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target = Views.extendValue(image, value);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, value, new DiamondShape(1), filter);
	}

}