package net.imglib2.labkit.control.brush;

import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.fill.Filter;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.control.brush.neighborhood.TransformedSphere;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.operators.ValueEquals;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Pair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

/**
 * A {@link TransformEventHandler} that changes an {@link AffineTransform3D}
 * through a set of {@link Behaviour}s.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Philipp Hanslovsky
 */
public class LabelBrushController
{

	private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

	final private ViewerPanel viewer;

	private final BitmapModel model;

	final private BrushOverlay brushOverlay;

	private int brushRadius = 5;

	boolean sliceTime;

	public BrushOverlay getBrushOverlay()
	{
		return brushOverlay;
	}

	public LabelBrushController(
			final ViewerPanel viewer,
			final BitmapModel model,
			final ActionsAndBehaviours behaviors,
			final boolean sliceTime)
	{
		this.viewer = viewer;
		this.brushOverlay = new BrushOverlay( viewer, model );
		this.sliceTime = sliceTime;
		this.model = model;
		brushOverlay.setRadius( ( int ) getBrushRadius() );

		behaviors.addBehaviour( new PaintBehavior(true), "paint", "D button1", "SPACE button1" );
		RunnableAction nop = new RunnableAction("nop", () -> { });
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour( new PaintBehavior(false), "erase", "E button1", "SPACE button2", "SPACE button3" );
		behaviors.addBehaviour( new FloodFillClick(true), "floodfill", "F button1" );
		behaviors.addBehaviour( new FloodFillClick(false), "floodclear", "R button1", "F button2", "F button3" );
		behaviors.addBehaviour( new ChangeBrushRadius(), "change brush radius", "D scroll", "E scroll", "SPACE scroll" );
		behaviors.addBehaviour( new MoveBrush(), "move brush", "E", "D", "SPACE" );
	}

	private RealPoint displayToImageCoordinates( final int x, final int y )
	{
		final RealPoint labelLocation = new RealPoint(3);
		labelLocation.setPosition( x, 0 );
		labelLocation.setPosition( y, 1 );
		labelLocation.setPosition( 0, 2 );
		viewer.displayToGlobalCoordinates( labelLocation );
		model.transformation().applyInverse( labelLocation, labelLocation );
		labelLocation.move( PIXEL_CENTER_OFFSET );
		return labelLocation;
	}

	private class PaintBehavior implements DragBehaviour
	{
		private boolean value;

		private RealPoint before;

		public PaintBehavior(boolean value) {
			this.value = value;
		}

		private void paint( RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				final RandomAccessible<BitType> extended = Views.extendValue( bitmap(), new BitType(false));
				double brushWidth = getBrushRadius() * getScale( viewerTransformation() );
				double brushDepth = brushWidth;
				AffineTransform3D D = brushMatrix( coords, brushWidth, brushDepth );
				AffineTransform3D m = displayToImageTransformation();
				m.concatenate( D );
				Neighborhood<BitType> neighborhood = TransformedSphere.asNeighborhood( new long[3], m, extended.randomAccess() );
				neighborhood.forEach(pixel -> pixel.set( value ));
			}

		}

		private AffineTransform3D brushMatrix( RealLocalizable coords, double brushWidth, double brushDepth )
		{
			AffineTransform3D D = new AffineTransform3D();
			D.set( brushWidth, 0.0, 0.0, coords.getDoublePosition( 0 ),
					0.0, brushWidth, 0.0, coords.getDoublePosition( 1 ),
					0.0, 0.0, brushDepth, 0.0);
			return D;
		}

		private AffineTransform3D displayToImageTransformation()
		{
			AffineTransform3D m = new AffineTransform3D();
			m.concatenate( model.transformation().inverse() );
			m.concatenate( viewerTransformation().inverse() );
			m.translate( PIXEL_CENTER_OFFSET );
			return m;
		}

		private AffineTransform3D viewerTransformation()
		{
			AffineTransform3D t = new AffineTransform3D();
			viewer.getState().getViewerTransform( t );
			return t;
		}

		private void paint(RealLocalizable a, RealLocalizable b) {
			long distance = (long) distance(a, b) + 1;
			double step = Math.max(brushRadius * 0.5, 1.0);
			for ( long i = 0; i < distance; i += step )
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
			RealPoint coords = new RealPoint(x, y);
			this.before = coords;
			paint(coords);

			fireBitmapChanged();
		}

		@Override
		public void drag( final int x, final int y )
		{
			RealPoint coords = new RealPoint(x, y);
			paint(before, coords );
			this.before = coords;
			brushOverlay.setPosition( x, y );
			fireBitmapChanged();
		}

		@Override
		public void end( final int x, final int y )
		{
		}
	}

	private double getBrushRadius()
	{
		return brushRadius * getScale( model.transformation() ) + 0.5;
	}

	// TODO: find a good place
	private double getScale( AffineTransform3D transformation )
	{
		return IntStream.range( 0, 3 ).mapToDouble( i -> Affine3DHelpers.extractScale( transformation, i ) ).reduce( 0, Math::max );
	}

	private RandomAccessibleInterval< BitType > bitmap()
	{
		if(!model.isValid())
			return ArrayImgs.bits(1,1,1);
		RandomAccessibleInterval<BitType> label = model.bitmap();
		if(sliceTime)
			return Views.hyperSlice(label, label.numDimensions()-1,
					viewer.getState().getCurrentTimepoint());
		return label;
	}

	private void fireBitmapChanged()
	{
		model.fireBitmapChanged();
	}

	private class ChangeBrushRadius implements ScrollBehaviour
	{
		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			if ( !isHorizontal )
			{
				int sign = ( wheelRotation < 0 ) ? 1 : -1;
				int distance = Math.max( 1, (int) (brushRadius * 0.1) );
				brushRadius = Math.min(Math.max( 0, brushRadius + sign * distance ), 50);
				brushOverlay.setRadius( ( int ) getBrushRadius() );
				brushOverlay.requestRepaint();
			}
		}
	}

	private class MoveBrush implements DragBehaviour
	{

		@Override
		public void init( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );
			brushOverlay.setVisible( true );
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
			brushOverlay.requestRepaint();
		}

		@Override
		public void drag( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );
		}

		@Override
		public void end( final int x, final int y )
		{
			brushOverlay.setVisible( false );
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
			brushOverlay.requestRepaint();
		}
	}

	private class FloodFillClick implements ClickBehaviour
	{
		private final boolean value;

		FloodFillClick(boolean value) {
			this.value = value;
		}

		protected void floodFill( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				RandomAccessibleInterval<BitType> bitmap = bitmap();
				Point seed = roundAndReduceDimension(coords, bitmap.numDimensions());
				LabelBrushController.floodFill( bitmap, seed, new BitType(value));
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
	}

	public static <T extends Type<T> & ValueEquals<T>> void floodFill(RandomAccessibleInterval<T> image, Localizable seed, T value) {
		Filter<Pair<T, T>, Pair<T, T>> filter = (f, s) -> ! value.valueEquals(f.getB());
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target = Views.extendValue(image, value);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, value, new DiamondShape(1), filter);
	}
}
