
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;

import javax.swing.*;
import java.util.function.Consumer;

public interface Extensible {

	Context context();

	<T> void addMenuItem(MenuKey<T> key, String title, float priority,
		Consumer<T> action, Icon icon, String keyStroke);

	JFrame dialogParent();

	ProgressConsumer progressConsumer();
}
