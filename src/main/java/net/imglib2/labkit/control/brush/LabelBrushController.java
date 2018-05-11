package net.imglib2.labkit.control.brush;

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
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactories;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactory;
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
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private final NeighborhoodFactory pixelsGenerator =
		NeighborhoodFactories.hyperSphere();

	final private BrushOverlay brushOverlay;

	private int brushRadius = 5;

	boolean sliceTime;

	private final PaintBehavior painter;
	private final PaintBehavior eraser;
	private final FloodFillClick floodFill;
	private final FloodFillClick floodClear;

	private ActionsAndBehaviours behaviours;

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
		this.behaviours = behaviors;

		painter = new PaintBehavior( "paint", true);
		eraser = new PaintBehavior("erase", false);
		floodFill = new FloodFillClick( "floodfill", true);
		floodClear = new FloodFillClick("floodclear", false);

		behaviors.addBehaviour( painter, painter.getName(), "D button1", "SPACE button1" );
		RunnableAction nop = new RunnableAction("nop", () -> { });
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour( eraser, eraser.getName(), "E button1", "SPACE button2", "SPACE button3" );
		behaviors.addBehaviour( floodFill, floodFill.getName(), "F button1" );
		behaviors.addBehaviour( floodClear, "floodclear", "R button1", "F button2", "F button3" );
		behaviors.addBehaviour( new ChangeBrushRadius(), "change brush radius", "D scroll", "E scroll", "SPACE scroll" );
		behaviors.addBehaviour( new MoveBrush(), "move brush", "E", "D", "SPACE" );

		List<NamedDragBehaviour> dragBehaviours = new ArrayList<>();
		List<NamedClickBehaviour> clickBehaviours = new ArrayList<>();
		dragBehaviours.add(painter);
		dragBehaviours.add(eraser);
		clickBehaviours.add(floodFill);
		clickBehaviours.add(floodClear);

		clickBehaviours.forEach(behaviour -> {
			behaviors.addAction(new AbstractNamedAction(behaviour.getName()) {
				{
					putValue(Action.SELECTED_KEY, false);
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean selected = (boolean) getValue(Action.SELECTED_KEY);
					System.out.println(selected);
					behaviour.setEnabled(selected);
				}
			});
		});

		dragBehaviours.forEach(behaviour -> {
			behaviors.addAction(new AbstractNamedAction(behaviour.getName()) {
				{
					putValue(Action.SELECTED_KEY, false);
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean selected = (boolean) getValue(Action.SELECTED_KEY);
					System.out.println(selected);
					behaviour.setEnabled(selected);
				}
			});
		});

		viewer.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				clickBehaviours.forEach(behaviour -> {
					if (behaviour.isEnabled()) {
						behaviour.click(e.getX(), e.getY());
					}
				});
				super.mouseClicked(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragBehaviours.forEach(behaviour -> {
					if (behaviour.isEnabled()) {
						behaviour.end(e.getX(), e.getY());
					}
				});
				super.mouseReleased(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				dragBehaviours.forEach(behaviour -> {
					if (behaviour.isEnabled()) {
						behaviour.drag(e.getX(), e.getY());
					}
				});
				super.mouseDragged(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				//TODO
				super.mouseMoved(e);
			}
		});

		Map<Character, DisabableBehaviour> keymap = new HashMap<>();
		keymap.put('D', painter);
		keymap.put('E', eraser);
		keymap.put('F', floodFill);
		keymap.put('R', floodClear);

		viewer.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				keymap.forEach((key, behaviour) -> {
					if(e.getKeyChar() == key) {
						behaviour.setEnabled(true);
					}
				});
			}
			@Override
			public void keyReleased(KeyEvent e) {
				keymap.forEach((key, behaviour) -> {
					if(e.getKeyChar() == key) {
						behaviour.setEnabled(false);
					}
				});
			}
		});
	}

	private RealPoint displayToImageCoordinates( final int x, final int y )
	{
		final RealPoint labelLocation = new RealPoint(3);
		labelLocation.setPosition( x, 0 );
		labelLocation.setPosition( y, 1 );
		labelLocation.setPosition( 0, 2 );
		viewer.displayToGlobalCoordinates( labelLocation );
		labelLocation.move( PIXEL_CENTER_OFFSET );
		return labelLocation;
	}

	private interface DisabableBehaviour {
		void setEnabled(boolean enabled);
		void toggleEnabled();
		boolean isEnabled();
	}

	private interface NamedBehaviour {
		public String getName();
	}

	private interface NamedClickBehaviour extends ClickBehaviour, DisabableBehaviour, NamedBehaviour {}

	private interface NamedDragBehaviour extends DragBehaviour, DisabableBehaviour, NamedBehaviour {}

	private class PaintBehavior implements NamedDragBehaviour
	{
		String name;

		private boolean enabled = false;
		private boolean initiated = false;

		private boolean value;

		private RealPoint before;

		public PaintBehavior(String name, boolean value) {
			this.name = name;
			this.value = value;
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
						toLongArray(coords, extended.numDimensions()), brushRadius);
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
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void toggleEnabled() {
			enabled = !enabled;
		}

		@Override
		public boolean isEnabled() { return enabled; }

		@Override
		public void init( final int x, final int y )
		{
			initiated = true;
			behaviours.getActions().get(getName()).actionPerformed(new ActionEvent(this, 0, ""));
			RealPoint coords = displayToImageCoordinates(x, y);
			this.before = coords;
			paint(coords);

			fireBitmapChanged();
		}

		@Override
		public void drag( final int x, final int y )
		{
			if(!initiated) init(x,y);
			RealPoint coords = displayToImageCoordinates(x, y);
			paint(before, coords );
			this.before = coords;
			brushOverlay.setPosition( x, y );
			fireBitmapChanged();
		}

		@Override
		public void end( final int x, final int y )
		{
			initiated = false;
		}

		@Override
		public String getName() {
			return name;
		}
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
				brushOverlay.setRadius( brushRadius );
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

	private class FloodFillClick implements NamedClickBehaviour
	{

		private String name;

		private final boolean value;

		private boolean enabled = false;

		FloodFillClick(String name, boolean value) {
			this.name = name;
			this.value = value;
		}

		protected void floodFill( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				RandomAccessibleInterval<BitType> region = model.bitmap();
				Point seed = roundAndReduceDimension(coords, region.numDimensions());
				LabelBrushController.floodFill(region, seed, new BitType(value));
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
			if(!enabled) {
				behaviours.getActions().get(getName()).actionPerformed(new ActionEvent(this, 0, ""));
			}
			floodFill( displayToImageCoordinates(x, y) );
			fireBitmapChanged();
		}

		@Override
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void toggleEnabled() {
			enabled = !enabled;
		}

		@Override
		public boolean isEnabled() { return enabled; }

		@Override
		public String getName() {
			return name;
		}
	}

	public static <T extends Type<T> & ValueEquals<T>> void floodFill(RandomAccessibleInterval<T> image, Localizable seed, T value) {
		Filter<Pair<T, T>, Pair<T, T>> filter = (f, s) -> ! value.valueEquals(f.getB());
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target = Views.extendValue(image, value);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, value, new DiamondShape(1), filter);
	}
}
