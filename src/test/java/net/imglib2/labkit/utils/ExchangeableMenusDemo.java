
package net.imglib2.labkit.utils;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class ExchangeableMenusDemo {

	private static boolean toggle;

	public static void main(String... args) {
		JFrame frame = new JFrame();
		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(new JMenu("Permanent"));
		ExchangeableMenus exchangeableMenus = new ExchangeableMenus(bar);
		List<JMenu> a = Collections.singletonList(new JMenu("a"));
		List<JMenu> b = Collections.singletonList(new JMenu("b"));

		JButton button = new JButton("swap");
		button.addActionListener(ignore -> {
			toggle = !toggle;
			exchangeableMenus.replace(toggle ? a : b);
		});
		frame.add(button);
		frame.pack();
		frame.setVisible(true);
	}
}
