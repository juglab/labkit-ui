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

package sc.fiji.labkit.ui;

import sc.fiji.labkit.ui.menu.MenuKey;

import javax.swing.*;
import java.util.function.Function;

/**
 * @author Matthias Arzt
 */
public class MenuBar extends JMenuBar {

	public static final MenuKey<Void> LABELING_MENU = new MenuKey<>(Void.class);
	public static final MenuKey<Void> SEGMENTER_MENU = new MenuKey<>(Void.class);
	public static final MenuKey<Void> VIEW_MENU = new MenuKey<>(Void.class);
	public static final MenuKey<Void> OTHERS_MENU = new MenuKey<>(Void.class);
	public static final MenuKey<Void> HELP_MENU = new MenuKey<>(Void.class);

	public MenuBar(Function<MenuKey<Void>, JMenu> menuFactory) {
		addMenu(menuFactory, LABELING_MENU, "Labeling");
		addMenu(menuFactory, SEGMENTER_MENU, "Segmentation");
		addMenu(menuFactory, VIEW_MENU, "View");
		addMenu(menuFactory, OTHERS_MENU, "Others");
		addMenu(menuFactory, HELP_MENU, "Help");
	}

	private void addMenu(Function<MenuKey<Void>, JMenu> menuFactory,
		MenuKey<Void> key, String text)
	{
		final JMenu apply = menuFactory.apply(key);
		apply.setText(text);
		add(apply);
	}
}
