package sc.fiji.labkit.ui;

import org.scijava.Context;
import org.scijava.plugin.PluginService;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider.Scope;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;

import bdv.ui.keymap.KeymapManager;

public class LabKitKeymapManager extends KeymapManager
{

	private static final String LABKIT_KEYMAP_DIR = System.getProperty( "user.home" ) + "/.labkit/keymaps";

	/** The key-config scope for LabKit actions. */
	public static final Scope LABKIT_SCOPE = new Scope( "sc.fiji.labkit" );

	/** The key-config context for LabKit actions. */
	public static final String LABKIT_CONTEXT = "labkit";

	public LabKitKeymapManager()
	{
		super( LABKIT_KEYMAP_DIR );
	}

	/**
	 * Discover all {@code CommandDescriptionProvider}s with the LabKit scope.
	 */
	@Override
	public synchronized void discoverCommandDescriptions()
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		try (final Context context = new Context( PluginService.class ))
		{
			context.inject( builder );
			builder.discoverProviders( LABKIT_SCOPE );
			context.dispose();
			setCommandDescriptions( builder.build() );
		}
	}
}
