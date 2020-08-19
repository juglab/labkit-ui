
package net.imglib2.labkit.utils;

import javax.swing.*;
import java.util.Collection;

public class ExchangeableMenus {

	private final JMenuBar menuBar;

	private Collection<JMenu> current;

	public ExchangeableMenus(JMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public Collection<JMenu> get() {
		return current;
	}

	public void replace(Collection<JMenu> menus) {
		remove();
		this.current = menus;
		add();
		this.menuBar.revalidate();
		this.menuBar.repaint();
	}

	private void add() {
		if (current == null)
			return;
		for (JMenu menu : current) {
			this.menuBar.add(menu);
		}
	}

	private void remove() {
		if (current == null)
			return;
		for (JMenu menu : current) {
			this.menuBar.remove(menu);
		}
	}
}
