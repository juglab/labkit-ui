package net.imglib2.atlas;

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
import bdv.viewer.VisibilityAndGrouping;
import net.imglib2.ui.OverlayRenderer;

public class MouseWheelChannelSelector implements ScrollBehaviour
{

	private final ViewerPanel viewer;

	private final int numChannels;

	private boolean visible = false;

	private final Overlay overlay;

	public MouseWheelChannelSelector( final ViewerPanel viewer, final int numChannels )
	{
		super();
		this.viewer = viewer;
		this.overlay = new Overlay();
		this.numChannels = numChannels;
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
				final VisibilityAndGrouping vag = viewer.getVisibilityAndGrouping();
				final int currentGroup = vag.getCurrentGroup();
				if ( wheelRotation < 0 )
					vag.setCurrentGroup( Math.min( currentGroup + 1, numChannels - 1 ) );
				else if ( wheelRotation > 0 )
					vag.setCurrentGroup( Math.max( currentGroup - 1, 0 ) );

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
					final VisibilityAndGrouping vag = viewer.getVisibilityAndGrouping();
					final int currentGroup = vag.getCurrentGroup();
					final FontMetrics fm = g.getFontMetrics();
					final String str = vag.getSourceGroups().get( currentGroup ).getName();
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
