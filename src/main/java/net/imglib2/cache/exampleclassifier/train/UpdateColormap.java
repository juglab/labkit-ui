package net.imglib2.cache.exampleclassifier.train;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Random;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import gnu.trove.map.hash.TIntIntHashMap;

public class UpdateColormap extends AbstractNamedAction
{

	public static int alpha( final float alpha )
	{
		assert alpha >= 0.0 && alpha <= 1.0;
		final int val = Math.round( alpha * 255 );
		return val << 24;
	}

	public static int getMask( final float alpha )
	{
		return alpha( alpha ) | 255 << 16 | 255 << 8 | 255 << 0;
	}

	private final TIntIntHashMap cmap;

	private final int nLabels;

	private final Random rng;

	private final ViewerPanel viewer;

	private final int alphaMask;

	public static int ALPHA_BITS = 255 << 24;

	public UpdateColormap( final TIntIntHashMap cmap, final int nLabels, final Random rng, final ViewerPanel viewer, final float alpha )
	{
		super( "Updated color map." );
		this.cmap = cmap;
		this.nLabels = nLabels;
		this.rng = rng;
		this.viewer = viewer;
		this.alphaMask = getMask( alpha );
	}

	public void updateColormap()
	{
		System.out.println( Integer.toBinaryString( ALPHA_BITS ) + " " + Integer.toBinaryString( alphaMask ) );
		for ( int i = 0; i < nLabels; ++i )
			//			final double saturation = 1.0;
//			final double value = 1.0;
//			final double hue = rng.nextDouble();
//			cmap.put( i, ( rng.nextInt() | ALPHA_BITS ) & alphaMask );
			cmap.put( i, Color.HSBtoRGB( rng.nextFloat(), 1.0f, 1.0f ) );
	}

	@Override
	public void actionPerformed( final ActionEvent arg0 )
	{
		updateColormap();
		viewer.requestRepaint();
	}

}
