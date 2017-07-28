package net.imglib2.atlas;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class MenuBar extends JMenuBar {

	private JMenu menu = new JMenu("Menu");

	MenuBar() {
		add(menu);
	}

	public void add(AbstractNamedAction action) {
		menu.add(new JMenuItem(action));
	}
}
