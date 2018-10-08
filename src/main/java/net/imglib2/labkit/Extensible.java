
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;

import javax.swing.*;
import java.util.function.Consumer;

public interface Extensible {

	Context context();

	void addAction(String title, String command, Runnable action,
		String keyStroke);

	< T > void addMenuItem(MenuKey<T> key, String title, Consumer<T> action, Icon icon);

	JFrame dialogParent();

	ProgressConsumer progressConsumer();
}
