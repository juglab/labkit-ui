package net.imglib2.labkit.color;

import java.awt.event.ActionEvent;
import java.util.List;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;

public class UpdateColormap extends AbstractNamedAction
{

	private final List<String> labels;

	public static int alpha(final float alpha )
	{
		assert alpha >= 0.0 && alpha <= 1.0;
		final int val = Math.round( alpha * 255 );
		return val << 24;
	}

	public static int getMask( final float alpha )
	{
		return alpha( alpha ) | 255 << 16 | 255 << 8 | 255 << 0;
	}

	private final ColorMapProvider colorProvider;

	private final ViewerPanel viewer;

	public UpdateColormap(final ColorMapProvider colorProvider, final List<String> labels, final ViewerPanel viewer, final float alpha)
	{
		super( "Update Color Map" );
		this.colorProvider = colorProvider;
		this.labels = labels;
		this.viewer = viewer;
	}

	public void updateColormap()
	{
		// TODO
	}

	@Override
	public void actionPerformed( final ActionEvent arg0 )
	{
		updateColormap();
		viewer.requestRepaint();
	}

}
