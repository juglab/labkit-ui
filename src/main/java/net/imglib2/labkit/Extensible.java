
package net.imglib2.labkit;

import net.imglib2.labkit.menu.MenuKey;
import bdv.export.ProgressWriter;
import org.scijava.Context;

import javax.swing.*;
import java.util.function.Consumer;

public interface Extensible {

	Context context();

	<T> void addMenuItem(MenuKey<T> key, String title, float priority,
		Consumer<T> action, Icon icon, String keyStroke);

	JFrame dialogParent();
}
