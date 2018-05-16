package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

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

	private final BitmapModel model;

	private int brushRadius = 5;

	final private BrushOverlay brushOverlay;

	public BrushOverlay getBrushOverlay()
	{
		return brushOverlay;
	}

	final ActionsAndBehaviours behaviors;

	public LabelBrushController(
			final ViewerPanel viewer,
			final BitmapModel model,
			final ActionsAndBehaviours behaviors,
			final boolean sliceTime)
	{
		this.viewer = viewer;
		this.model = model;
		this.behaviors = behaviors;

		this.brushOverlay = new BrushOverlay( viewer, model );

		behaviors.addBehaviour(new PaintBehavior(true, viewer, model, sliceTime, brushOverlay), "paint", "D button1", "SPACE button1" );
		RunnableAction nop = new RunnableAction("nop", () -> { });
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour( new PaintBehavior(false, viewer, model, sliceTime, brushOverlay), "erase", "E button1", "SPACE button2", "SPACE button3" );
		behaviors.addBehaviour( new FloodFillClick(true, viewer, model), "floodfill", "F button1" );
		behaviors.addBehaviour( new FloodFillClick(false, viewer, model), "floodclear", "R button1", "F button2", "F button3" );
		behaviors.addBehaviour( new ChangeBrushRadius(), "change brush radius", "D scroll", "E scroll", "SPACE scroll" );
		behaviors.addBehaviour( new MoveBrush(brushOverlay, viewer), "move brush", "E", "D", "SPACE" );

	}

	public Behaviour getBehaviour(String name) {
		return behaviors.getBehaviour(name);
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

}
