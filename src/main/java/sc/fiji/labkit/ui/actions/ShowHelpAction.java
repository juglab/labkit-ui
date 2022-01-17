
package sc.fiji.labkit.ui.actions;

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ShowHelpAction {

	public ShowHelpAction(Extensible extensible) {
		extensible.addMenuItem(MenuBar.HELP_MENU,
			"Quick Start Tutorial",
			100,
			ignore -> showWebPage("https://imagej.net/plugins/labkit/pixel-classification-tutorial"),
			null, null);
		extensible.addMenuItem(MenuBar.HELP_MENU,
			"Online Documentation",
			101,
			ignore -> showWebPage("https://imagej.net/plugins/labkit"),
			null, null);
		extensible.addMenuItem(MenuBar.HELP_MENU,
			"Get Community Support",
			102,
			ignore -> showWebPage("https://forum.image.sc/tag/labkit"),
			null, null);
	}

	private void showWebPage(String url) {
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(new URI(url));
		}
		catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
