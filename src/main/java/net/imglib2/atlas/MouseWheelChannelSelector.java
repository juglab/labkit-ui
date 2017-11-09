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

import net.imglib2.ui.OverlayRenderer;

public class MouseWheelChannelSelector implements ScrollBehaviour
{

	private final Extensible extensible;

	private final FeatureLayer featureLayer;

	private boolean visible = false;

	private final Overlay overlay;

	public MouseWheelChannelSelector(final Extensible extensible, final FeatureLayer featureLayer )
	{
		super();
		this.extensible = extensible;
		this.overlay = new Overlay();
		this.featureLayer = featureLayer;
		extensible.addBehaviour(this, "mouseweheel selector", "shift F scroll");
		extensible.addBehaviour(overlay, "feature selector overlay", "shift F");
		extensible.addOverlayRenderer(overlay);
	}

	@Override
	public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
	{
		if ( !isHorizontal )
			synchronized(extensible.viewerSync()) {
				if ( wheelRotation < 0 )
					featureLayer.previous();
				else if ( wheelRotation > 0 )
					featureLayer.next();
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
					final String str = featureLayer.title(); //extensible.getState().getSources().get( activeChannel ).getSpimSource().getName();
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
			extensible.displayRepaint();
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
			extensible.displayRepaint();
		}
	}

}
