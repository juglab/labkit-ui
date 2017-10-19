package net.imglib2.atlas.control.brush;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.imglib2.*;
import net.imglib2.atlas.ActionsAndBehaviours;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.logic.BitType;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;

import bdv.viewer.ViewerPanel;
import net.imglib2.atlas.BrushOverlay;
import net.imglib2.atlas.color.ColorMap;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

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

	public static int BACKGROUND = -1;

	final protected ViewerPanel viewer;

	private List<RandomAccessibleInterval<BitType>> regions;

	private List<String> labels;

	private final PaintPixelsGenerator< BitType, ? extends Iterator<BitType> > pixelsGenerator;

	final protected AffineTransform3D labelTransform;

	final protected RealPoint labelLocation;

	final protected BrushOverlay brushOverlay;

	protected int brushRadius = 5;

	private int currentLabel = 0;

	public BrushOverlay getBrushOverlay()
	{
		return brushOverlay;
	}

	public int getCurrentLabel()
	{
		return currentLabel;
	}

	public void setCurrentLabel( final int label )
	{
		this.currentLabel = label;
	}

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	public LabelBrushController(
			final ViewerPanel viewer,
			final Holder<Labeling> labels,
			final PaintPixelsGenerator< BitType, ? extends Iterator<BitType>> pixelsGenerator,
			final ActionsAndBehaviours behaviors,
			final ColorMap colorProvider, AffineTransform3D labelTransform)
	{
		this.viewer = viewer;
		this.pixelsGenerator = pixelsGenerator;
		this.labelTransform = labelTransform;
		updateLabeling(labels.get());
		labels.notifier().add(this::updateLabeling);
		brushOverlay = new BrushOverlay( viewer, this.labels.get(currentLabel), colorProvider );

		labelLocation = new RealPoint( 3 );

		behaviors.addBehaviour( new Paint(), "paint", "SPACE button1" );
		behaviors.addBehaviour( new Erase(), "erase", "SPACE button2", "SPACE button3" );
		behaviors.addBehaviour( new ChangeBrushRadius(), "change brush radius", "SPACE scroll" );
		behaviors.addAction( new ChangeLabel(), "N" );
		behaviors.addBehaviour( new MoveBrush(), "move brush", "SPACE" );
	}

	void updateLabeling(Labeling labeling) {
		List<Map.Entry<String, RandomAccessibleInterval<BitType>>> entries =
				new ArrayList<>(labeling.regions().entrySet());
		this.labels = entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
		this.regions = entries.stream().map(Map.Entry::getValue).collect(Collectors.toList());
		currentLabel = Math.min(currentLabel, regions.size());
	}

	private void setCoordinates( final int x, final int y )
	{
		labelLocation.setPosition( x, 0 );
		labelLocation.setPosition( y, 1 );
		labelLocation.setPosition( 0, 2 );

		viewer.displayToGlobalCoordinates( labelLocation );

		labelTransform.applyInverse( labelLocation, labelLocation );
	}

	private abstract class AbstractPaintBehavior implements DragBehaviour
	{
		protected void paint( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				final int v = getValue();
				RandomAccessibleInterval<BitType> label = regions.get(v);
				final RandomAccessible<BitType> extended = Views.extendValue(label, new BitType(false));
				final Iterator< BitType > it = pixelsGenerator.getPaintPixels( extended, coords, viewer.getState().getCurrentTimepoint(), brushRadius );
				while ( it.hasNext() )
				{
					final BitType val = it.next();
					if ( Intervals.contains( label, ( Localizable ) it ) )
						val.set( doPaint() );
				}
			}

		}

		protected void paint( final int x, final int y )
		{
			setCoordinates( x, y );
			paint( labelLocation );
		}

		protected void paint( final int x1, final int y1, final int x2, final int y2 )
		{
			setCoordinates( x1, y1 );
			final double[] p1 = new double[ 3 ];
			final RealPoint rp1 = RealPoint.wrap( p1 );
			labelLocation.localize( p1 );

			setCoordinates( x2, y2 );
			final double[] d = new double[ 3 ];
			labelLocation.localize( d );

			LinAlgHelpers.subtract( d, p1, d );

			final double l = LinAlgHelpers.length( d );
			LinAlgHelpers.normalize( d );

			for ( int i = 1; i < l; ++i )
			{
				LinAlgHelpers.add( p1, d, p1 );
				paint( rp1 );
			}
			paint( labelLocation );
		}

		private int getValue()
		{
			return getCurrentLabel();
		}

		@Override
		public void init( final int x, final int y )
		{
			synchronized ( this )
			{
				oX = x;
				oY = y;
			}

			paint( x, y );

			viewer.requestRepaint();
		}

		@Override
		public void drag( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );

			paint( oX, oY, x, y );

			synchronized ( this )
			{
				oX = x;
				oY = y;
			}

			viewer.requestRepaint();
		}

		@Override
		public void end( final int x, final int y )
		{
		}

		protected abstract boolean doPaint();
	}

	private class Paint extends AbstractPaintBehavior
	{
		@Override
		protected boolean doPaint() {
			return true;
		}
	}

	private class Erase extends AbstractPaintBehavior
	{
		@Override
		protected boolean doPaint() {
			return false;
		}
	}

	private class ChangeBrushRadius implements ScrollBehaviour
	{
		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			if ( !isHorizontal )
			{
				if ( wheelRotation < 0 )
					brushRadius += 1;
				else if ( wheelRotation > 0 )
					brushRadius = Math.max( 0, brushRadius - 1 );

				brushOverlay.setRadius( brushRadius );
				// TODO request only overlays to repaint
				viewer.getDisplay().repaint();
			}
		}
	}

	private class ChangeLabel extends AbstractNamedAction {

		public ChangeLabel() {
			super("Next Label");
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			currentLabel = currentLabel == (regions.size() - 1) ? 0 : currentLabel + 1;
			brushOverlay.setLabel( labels.get(currentLabel) );
			// TODO request only overlays to repaint
			viewer.getDisplay().repaint();
		}
	}

	private class MoveBrush implements DragBehaviour
	{

		@Override
		public void init( final int x, final int y )
		{
			brushOverlay.setPosition( x, y );
			brushOverlay.setVisible( true );
			// TODO request only overlays to repaint
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
			viewer.getDisplay().repaint();
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
			// TODO request only overlays to repaint
			viewer.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
			viewer.getDisplay().repaint();

		}
	}
}
