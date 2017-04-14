package net.imglib2.atlas;

import java.awt.event.ActionEvent;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import bdv.viewer.state.SourceState;

public class ToggleVisibility extends AbstractNamedAction
{

	private final ViewerPanel viewer;

	private final int sourceIndex;

	public ToggleVisibility( final String name, final ViewerPanel viewer, final int sourceIndex )
	{
		super( name );
		this.viewer = viewer;
		this.sourceIndex = sourceIndex;
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		synchronized ( viewer )
		{
			final SourceState< ? > source = viewer.getVisibilityAndGrouping().getSources().get( sourceIndex );
			source.setActive( !source.isActive() );
			viewer.requestRepaint();
		}
	}

}
