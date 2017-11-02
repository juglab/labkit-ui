package net.imglib2.atlas.actions;

import java.awt.event.ActionEvent;

import bdv.util.BdvSource;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.viewer.ViewerPanel;
import bdv.viewer.state.SourceState;

import javax.swing.*;

public class ToggleVisibility extends AbstractNamedAction
{

	private final ViewerPanel viewer;

	private final SourceState<?> sourceState;

	public ToggleVisibility( final String name, final BdvSource source)
	{
		super( "Toggle " + name );
		putValue(ACTION_COMMAND_KEY, "toggle" + name);
		this.viewer = source.getBdvHandle().getViewerPanel();
		source.setCurrent();
		sourceState = viewer.getVisibilityAndGrouping().getSources().get( viewer.getVisibilityAndGrouping().getCurrentSource() );
		putValue(Action.SELECTED_KEY, sourceState.isActive());
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		synchronized ( viewer )
		{
			sourceState.setActive( !sourceState.isActive() );
			viewer.requestRepaint();
		}
		putValue(Action.SELECTED_KEY, sourceState.isActive());
	}

}
