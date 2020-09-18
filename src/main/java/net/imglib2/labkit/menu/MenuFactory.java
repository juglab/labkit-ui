
package net.imglib2.labkit.menu;

import net.imglib2.labkit.utils.Casts;
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
		return Casts.unchecked(menus.computeIfAbsent(key, k -> new ArrayList<>()));
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
