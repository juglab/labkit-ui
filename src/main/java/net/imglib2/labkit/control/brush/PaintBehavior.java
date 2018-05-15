package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactories;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactory;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.DragBehaviour;

import java.util.stream.IntStream;

public class PaintBehavior extends LabkitBrush implements DragBehaviour {

		private boolean value;

		private RealPoint before;

		private boolean sliceTime;

		private final NeighborhoodFactory pixelsGenerator =
			NeighborhoodFactories.hyperSphere();

		final private BrushOverlay brushOverlay;

		public PaintBehavior(boolean value, ViewerPanel viewer, BitmapModel model, boolean sliceTime, BrushOverlay brushOverlay) {
			super(viewer, model);
			this.value = value;
			this.sliceTime = sliceTime;
			this.brushOverlay = brushOverlay;
		}

		private void paint( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				RandomAccessibleInterval<BitType> label = model.bitmap();
				if(sliceTime)
					label = Views.hyperSlice(label, label.numDimensions()-1,
							viewer.getState().getCurrentTimepoint());
				final RandomAccessible<BitType> extended = Views.extendValue(label, new BitType(false));
				Neighborhood<BitType> neighborhood = pixelsGenerator.create(extended.randomAccess(),
						toLongArray(coords, extended.numDimensions()), brushOverlay.getRadius());
				neighborhood.forEach(pixel -> pixel.set( value ));
			}

		}

		private long[] toLongArray(RealLocalizable coords, int numDimensions) {
			return IntStream.range(0, numDimensions)
					.mapToLong(d -> (long) coords.getDoublePosition(d))
					.toArray();
		}

		private void paint(RealLocalizable a, RealLocalizable b) {
			long distance = (long) distance(a, b) + 1;
			for ( long i = 0; i <= distance; ++i )
				paint( interpolate((double) i / (double) distance, a, b) );
		}

		RealLocalizable interpolate(double ratio, RealLocalizable a, RealLocalizable b) {
			RealPoint result = new RealPoint(a.numDimensions());
			for (int d = 0; d < result.numDimensions(); d++)
				result.setPosition(ratio * a.getDoublePosition(d) + (1 - ratio) * b.getDoublePosition(d), d);
			return result;
		}

		double distance(RealLocalizable a, RealLocalizable b) {
			return LinAlgHelpers.distance(asArray(a), asArray(b));
		}

		private double[] asArray(RealLocalizable a) {
			double[] result = new double[a.numDimensions()];
			a.localize(result);
			return result;
		}

		@Override
		public void init( final int x, final int y )
		{
			RealPoint coords = displayToImageCoordinates(x, y);
			this.before = coords;
			paint(coords);

			fireBitmapChanged();
		}

		@Override
		public void drag( final int x, final int y )
		{
			RealPoint coords = displayToImageCoordinates(x, y);
			paint(before, coords );
			this.before = coords;
			brushOverlay.setPosition( x, y );
			fireBitmapChanged();
		}

		@Override
		public void end( final int x, final int y ) {}
	}
