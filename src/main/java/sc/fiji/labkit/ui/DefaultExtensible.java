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

import sc.fiji.labkit.ui.menu.MenuFactory;
import sc.fiji.labkit.ui.menu.MenuKey;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default implementation of {@link DefaultExtensible}.
 */
public class DefaultExtensible implements Extensible {

	private final Context context;
	private final JFrame dialogBoxOwner;
	private final MenuFactory menus = new MenuFactory();

	public DefaultExtensible(Context context, JFrame dialogBoxOwner) {
		this.context = context;
		this.dialogBoxOwner = dialogBoxOwner;
	}

	@Override
	public Context context() {
		return context;
	}

	@Override
	public <T> void addMenuItem(MenuKey<T> key, String title, float priority,
		Consumer<T> action, Icon icon, String keyStroke)
	{
		menus.addMenuItem(key, title, priority, action, icon, keyStroke);
	}

	@Override
	public JFrame dialogParent() {
		return dialogBoxOwner;
	}

	public <T> JPopupMenu createPopupMenu(MenuKey<T> key, Supplier<T> item) {
		return menus.createPopupMenu(key, item);
	}

	public <T> JMenu createMenu(MenuKey<T> key, Supplier<T> item) {
		return menus.createMenu(key, item);
	}

	public List<AbstractNamedAction> getShortCuts() {
		return menus.shortCutActions();
	}
}
