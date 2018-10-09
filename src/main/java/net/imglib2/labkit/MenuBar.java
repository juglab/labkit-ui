
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuKey;

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

	MenuBar(Function<MenuKey<Void>, JMenu> menuFactory) {
		addMenu(menuFactory, LABELING_MENU, "Labeling");
		addMenu(menuFactory, SEGMENTER_MENU, "Segmentation");
		addMenu(menuFactory, VIEW_MENU, "View");
		addMenu(menuFactory, OTHERS_MENU, "Others");
	}

	private void addMenu(Function<MenuKey<Void>, JMenu> menuFactory,
		MenuKey<Void> key, String text)
	{
		final JMenu apply = menuFactory.apply(key);
		apply.setText(text);
		add(apply);
	}
}
