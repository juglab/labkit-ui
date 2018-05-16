package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import org.scijava.ui.behaviour.DragBehaviour;

import java.awt.*;

public class MoveBrush implements DragBehaviour {

	final private BrushOverlay brushOverlay;
	final private ViewerPanel viewer;

	public MoveBrush(final BrushOverlay brushOverlay, final ViewerPanel viewer) {
		this.brushOverlay = brushOverlay;
		this.viewer = viewer;
	}

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