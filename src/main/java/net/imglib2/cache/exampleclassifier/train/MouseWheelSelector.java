package net.imglib2.cache.exampleclassifier.train;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;

import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ui.OverlayRenderer;

public class MouseWheelSelector implements ScrollBehaviour
{

	private final MouseWheelSelectorRandomAccessibleInterval< ? > rai;

	private final ViewerPanel viewer;

	private boolean visible = false;

	private final Overlay overlay;

	public MouseWheelSelector( final RandomAccessibleInterval< ? > rai, final int d, final ViewerPanel viewer )
	{
		this( new MouseWheelSelectorRandomAccessibleInterval<>( rai, d ), viewer );
	}

	public MouseWheelSelector( final MouseWheelSelectorRandomAccessibleInterval< ? > rai, final ViewerPanel viewer )
	{
		super();
		this.rai = rai;
		this.viewer = viewer;
		this.overlay = new Overlay();
	}

	public Overlay getOverlay()
	{
		return overlay;
	}

	@Override
	public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
	{
		if ( !isHorizontal )
			synchronized( viewer ) {
				if ( wheelRotation < 0 )
					rai.setsSlice( Math.min( rai.getSliceIndex() + 1, rai.getMaxSlice() ) );
				else if ( wheelRotation > 0 )
					rai.setsSlice( Math.max( rai.getSliceIndex() - 1, rai.getMinSlice() ) );

				viewer.requestRepaint();
			}
	}

	private class Overlay implements OverlayRenderer, DragBehaviour
	{

		private int x = 0;

		private int y = 0;

		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( visible )
			{
				final Graphics2D g2d = ( Graphics2D ) g;

				g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
				g2d.setComposite( AlphaComposite.SrcOver );

				final int xOff = 15;
				final int yOff = 5;

				final int x = this.x + xOff;
				final int y = this.y + yOff;

				{
					final FontMetrics fm = g.getFontMetrics();
					final String str = "Feature " + ( rai.getSliceIndex() - rai.getMinSlice() + 1 );
					final Rectangle2D rect = fm.getStringBounds( str, g );
					g2d.setColor( Color.WHITE );
					g2d.fillRect( x, y - fm.getAscent(), ( int ) rect.getWidth(), ( int ) rect.getHeight() );
					g2d.setColor( Color.BLACK );
					g2d.drawString( str, x, y );

				}
			}
		}

		@Override
		public void setCanvasSize( final int arg0, final int arg1 )
		{
			// don't do anything
		}

		@Override
		public void init( final int x, final int y )
		{
			visible = true;
			viewer.getDisplay().repaint();
			this.x = x;
			this.y = y;
		}

		@Override
		public void drag( final int x, final int y )
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public void end( final int x, final int y )
		{
			visible = false;
			viewer.getDisplay().repaint();

		}
	}

}
