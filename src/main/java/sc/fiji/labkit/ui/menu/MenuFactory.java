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

package sc.fiji.labkit.ui.menu;

import net.imglib2.util.Cast;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Holds a list of commands, together with the meta data required to generate a
 * menu from it.
 */
public class MenuFactory {

	private final ConcurrentMap<MenuKey<?>, List<Entry<?>>> menus =
		new ConcurrentHashMap<>();
	private final List<AbstractNamedAction> shortCutActions = new ArrayList<>();

	public <T> void addMenuItem(MenuKey<T> key, String title, float priority,
		Consumer<T> action, Icon icon, String keyStroke)
	{
		List<Entry<T>> list = getMenu(key);
		final Entry<T> entry = new Entry<>(title, priority, action, icon,
			keyStroke);
		list.add(entry);
		if (keyStroke != null && !keyStroke.isEmpty() && key
			.inputParameterClass() == Void.class) shortCutActions.add(entry.asAction(
				() -> null));
	}

	public <T> JPopupMenu createPopupMenu(MenuKey<T> key, Supplier<T> item) {
		JPopupMenu menu = new JPopupMenu();
		addMenuItems(key, item, menu::add, menu::addSeparator);
		return menu;
	}

	public <T> JMenu createMenu(MenuKey<T> key, Supplier<T> item) {
		JMenu menu = new JMenu("Hello");
		addMenuItems(key, item, menu::add, menu::addSeparator);
		return menu;
	}

	private <T> void addMenuItems(MenuKey<T> key, Supplier<T> item,
		Consumer<Action> addAction, Runnable addSeparator)
	{
		List<Entry<T>> list = new ArrayList<>(getMenu(key));
		list.sort(Comparator.comparing(entry -> entry.priority));
		int previous = list.isEmpty() ? 0 : group(list.get(0));
		for (Entry<T> entry : list) {
			int group = group(entry);
			if (group != previous) addSeparator.run();
			previous = group;
			addAction.accept(entry.asAction(item));
		}
	}

	private <T> int group(Entry<T> entry) {
		return (int) (entry.priority / 100);
	}

	private <T> List<Entry<T>> getMenu(MenuKey<T> key) {
		Object input = menus.computeIfAbsent(key, k -> new ArrayList<>());
		return Cast.unchecked(input);
	}

	private static final AtomicInteger id = new AtomicInteger();

	public List<AbstractNamedAction> shortCutActions() {
		return shortCutActions;
	}

	private static class Entry<T> {

		private final String title;
		private final float priority;
		private final Consumer<T> action;
		private final Icon icon;
		private final String keyStroke;
		private final String commandKey;

		private Entry(String title, float priority, Consumer<T> action, Icon icon,
			String keyStroke)
		{
			this.title = title;
			this.priority = priority;
			this.action = action;
			this.icon = icon;
			this.keyStroke = keyStroke;
			this.commandKey = "MenuFactoryGeneratedCommandKey" + id.getAndIncrement();
		}

		private RunnableAction asAction(Supplier<T> item) {
			RunnableAction action = new RunnableAction(title, () -> this.action
				.accept(item.get()));
			action.putValue(Action.SMALL_ICON, this.icon);
			action.putValue(Action.LARGE_ICON_KEY, this.icon);
			action.putValue(Action.ACTION_COMMAND_KEY, this.commandKey);
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				this.keyStroke));
			return action;
		}
	}
}
