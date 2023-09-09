package sc.fiji.labkit.ui.actions;

import java.awt.event.ActionEvent;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

import sc.fiji.labkit.ui.LabKitKeymapManager;

public class ExampleAction extends AbstractNamedAction
{

	private static final long serialVersionUID = 1L;

	public static final String ACTION_NAME = "example action";

	public static final String[] ACTION_DEFAULT_KEYS = new String[] { "A" };

	public static final String ACTION_DESCRIPTION = "Print a useless message.";

	public ExampleAction( final Actions actions )
	{
		super( ACTION_NAME );
		actions.namedAction( this, ACTION_DEFAULT_KEYS );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		System.out.println( "TROLOLO" ); // DEBUG
	}

	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( LabKitKeymapManager.LABKIT_SCOPE, LabKitKeymapManager.LABKIT_CONTEXT );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( ACTION_NAME, ACTION_DEFAULT_KEYS, ACTION_DESCRIPTION );
		}
	}
}
