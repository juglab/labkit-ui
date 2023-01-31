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
