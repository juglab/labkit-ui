package net.imglib2.labkit.control.brush;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.imglib2.*;
import net.imglib2.algorithm.fill.Filter;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactories;
import net.imglib2.labkit.control.brush.neighborhood.NeighborhoodFactory;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.operators.ValueEquals;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Pair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;

import bdv.viewer.ViewerPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;

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

	final private ViewerPanel viewer;

	private Map<String, RandomAccessibleInterval<BitType>> regions;

	private List<String> labels;

	private final NeighborhoodFactory pixelsGenerator =
		NeighborhoodFactories.hyperSphere();

	final private BrushOverlay brushOverlay;

	private int brushRadius = 5;

	private final Holder<String> selectedLabel;

	boolean sliceTime;

	public BrushOverlay getBrushOverlay()
	{
		return brushOverlay;
	}

	private String getCurrentLabel()
	{
		return selectedLabel.get();
	}

	private void setCurrentLabel(String label) {
		brushOverlay.setLabel(label);
		selectedLabel.set(label);
	}

	public LabelBrushController(
			final ViewerPanel viewer,
			final LabelingModel model,
			final ActionsAndBehaviours behaviors,
			final boolean sliceTime)
	{
		this.viewer = viewer;
		this.brushOverlay = new BrushOverlay( viewer, "", model.colorMapProvider() );
		this.sliceTime = sliceTime;
		updateLabeling(model.labeling().get());
		model.labeling().notifier().add(this::updateLabeling);
		selectedLabel = model.selectedLabel();
		setCurrentLabel(selectedLabel.get());
		selectedLabel.notifier().add( label -> setCurrentLabel(label) );

		behaviors.addBehaviour( new PaintBehavior(true), "paint", "D button1", "SPACE button1" );
		RunnableAction nop = new RunnableAction("nop", () -> { });
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour( new PaintBehavior(false), "erase", "E button1", "SPACE button2", "SPACE button3" );
		behaviors.addBehaviour( new FloodFillClick(true), "floodfill", "F button1" );
		behaviors.addBehaviour( new FloodFillClick(false), "floodclear", "R button1", "F button2", "F button3" );
		behaviors.addBehaviour( new ChangeBrushRadius(), "change brush radius", "D scroll", "E scroll", "SPACE scroll" );
		behaviors.addAction( new ChangeLabel() );
		behaviors.addBehaviour( new MoveBrush(), "move brush", "E", "D", "SPACE" );
	}

	void updateLabeling(Labeling labeling) {
		Map<String, RandomAccessibleInterval<BitType>> regions = new HashMap<>(labeling.regions());
		if(regions.isEmpty()) {
			RandomAccessibleInterval<BitType> dummy = ConstantUtils.constantRandomAccessibleInterval(new BitType(), labeling.numDimensions(), labeling);
			regions = Collections.singletonMap("no label", dummy);
		}
		this.labels = new ArrayList<>(regions.keySet());
		this.regions = regions;
	}

	private RealPoint displayToImageCoordinates( final int x, final int y )
	{
		final RealPoint labelLocation = new RealPoint(3);
		labelLocation.setPosition( x, 0 );
		labelLocation.setPosition( y, 1 );
		labelLocation.setPosition( 0, 2 );
		viewer.displayToGlobalCoordinates( labelLocation );
		return labelLocation;
	}

	private class PaintBehavior implements DragBehaviour
	{
		private boolean value;

		private RealPoint before;

		public PaintBehavior(boolean value) {
			this.value = value;
		}

		private void paint( final RealLocalizable coords)
		{
			synchronized ( viewer )
			{
				RandomAccessibleInterval<BitType> label = regions.get(getCurrentLabel());
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
		public void init( final int x, final int y )
		{
			RealPoint coords = displayToImageCoordinates(x, y);
			this.before = coords;
			paint(coords);

			viewer.requestRepaint();
		}

		@Override
		public void drag( final int x, final int y )
		{
			RealPoint coords = displayToImageCoordinates(x, y);
			paint(before, coords );
			this.before = coords;
			brushOverlay.setPosition( x, y );
			viewer.requestRepaint();
		}

		@Override
		public void end( final int x, final int y )
		{
		}
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
				// TODO request only overlays to repaint
				viewer.getDisplay().repaint();
			}
		}
	}

	private class ChangeLabel extends AbstractNamedAction {

		public ChangeLabel() {
			super("Next Label");
			super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("N"));
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			setCurrentLabel(next(labels, getCurrentLabel()));
			// TODO request only overlays to repaint
			viewer.getDisplay().repaint();
		}

		private String next(List<String> labels, String currentLabel) {
			if(labels.isEmpty())
				return null;
			int index = labels.indexOf(currentLabel) + 1;
			if(index >= labels.size()) index = 0;
			return labels.get(index);
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
				RandomAccessibleInterval<BitType> region = regions.get(getCurrentLabel());
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
			floodFill( displayToImageCoordinates(x, y) );
			viewer.requestRepaint();
		}
	}

	public static <T extends Type<T> & ValueEquals<T>> void floodFill(RandomAccessibleInterval<T> image, Localizable seed, T value) {
		Filter<Pair<T, T>, Pair<T, T>> filter = (f, s) -> ! value.valueEquals(f.getB());
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target = Views.extendValue(image, value);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, value, new DiamondShape(1), filter);
	}
}
