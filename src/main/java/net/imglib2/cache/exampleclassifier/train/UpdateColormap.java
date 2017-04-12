package net.imglib2.cache.exampleclassifier.train;

import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.stream.IntStream;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import net.imglib2.cache.exampleclassifier.train.color.ColorMapColorProvider;

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

	private final ColorMapColorProvider< ? > colorProvider;

	private final int nLabels;

	private final Random rng;

	private final ViewerPanel viewer;


	public UpdateColormap( final ColorMapColorProvider< ? > colorProvider, final int nLabels, final Random rng, final ViewerPanel viewer, final float alpha )
	{
		super( "color map updater" );
		this.colorProvider = colorProvider;
		this.nLabels = nLabels;
		this.rng = rng;
		this.viewer = viewer;
	}

	public void updateColormap()
	{
		colorProvider.setColors( rng, IntStream.range( 0, nLabels ).toArray() );
	}

	@Override
	public void actionPerformed( final ActionEvent arg0 )
	{
		updateColormap();
		viewer.requestRepaint();
	}

}
