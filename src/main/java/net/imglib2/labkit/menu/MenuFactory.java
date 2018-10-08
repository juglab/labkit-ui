package net.imglib2.labkit.menu;

import net.imglib2.trainable_segmention.RevampUtils;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MenuFactory {

	private final ConcurrentMap<MenuKey<?>, List<Entry<?>> > menus = new ConcurrentHashMap<>();

	public < T > void addMenuItem(MenuKey< T > key, String title,
			Consumer< T > action, Icon icon)
	{
		List< Entry< ? > > list = menus.computeIfAbsent(key, k -> new ArrayList<>());
		list.add(new Entry<>(title, action, icon));
	}

	public <T> JPopupMenu createMenu( MenuKey< T > key, Supplier< T > item ) {
		List< Entry< T > > list = RevampUtils
				.uncheckedCast( menus.computeIfAbsent(key, k -> new ArrayList<>()) );
		JPopupMenu menu = new JPopupMenu();
		for (Entry< T > entry : list) {
			RunnableAction action = new RunnableAction(entry.title,
					() -> entry.action.accept(item.get()));
			action.putValue(Action.SMALL_ICON, entry.icon);
			action.putValue(Action.LARGE_ICON_KEY, entry.icon);
			menu.add(action);
		}
		return menu;
	}

	private static class Entry<T> {

		private final String title;
		private final Consumer<T> action;
		private final Icon icon;

		private Entry(String title, Consumer<T> action, Icon icon) {
			this.title = title;
			this.action = action;
			this.icon = icon;
		}
	}
}
