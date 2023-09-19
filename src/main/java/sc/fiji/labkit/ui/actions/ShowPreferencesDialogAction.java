/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.actions;

import java.awt.Frame;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.PreferencesDialog;
import bdv.tools.ToggleDialogAction;
import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.ui.keymap.KeymapSettingsPage;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.LabKitKeymapManager;
import sc.fiji.labkit.ui.MenuBar;

public class ShowPreferencesDialogAction
{

	public static final String ACTION_NAME = "show preferences dialog";

	public static final String[] ACTION_DEFAULT_KEYS = new String[] { "control P" };

	public static final String ACTION_DESCRIPTION = "Shows the preferences dialog.";

	public static void install(Actions actions, Extensible extensible, KeymapManager keymapManager, Frame owner) {
		final Keymap keymap = keymapManager.getForwardSelectedKeymap();
		final PreferencesDialog preferencesDialog = new PreferencesDialog(owner, keymap,
				new String[] { LabKitKeymapManager.LABKIT_CONTEXT });
		preferencesDialog
				.addPage(new KeymapSettingsPage("Keymap", keymapManager, keymapManager.getCommandDescriptions()));
		final ToggleDialogAction action = new ToggleDialogAction(ACTION_NAME, preferencesDialog);
		actions.namedAction(action, ACTION_DEFAULT_KEYS);
		extensible.addMenuItem( MenuBar.HELP_MENU,
				"Preferences",
				99,
				ignore -> action.actionPerformed( null ),
				null, null );
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
			descriptions.add( "close dialog window", new String[] { "control W" }, "Closes the preferences dialog." );
		}
	}
}
