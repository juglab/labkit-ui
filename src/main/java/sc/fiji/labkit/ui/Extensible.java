
package sc.fiji.labkit.ui;

import sc.fiji.labkit.ui.menu.MenuKey;
import org.scijava.Context;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Interface that is used by actions / commands. A command can register for a
 * menu item. A {@link #dialogParent()} is provided. This can be used by the
 * command to show for example a file open dialog.
 */
public interface Extensible {

	Context context();

	<T> void addMenuItem(MenuKey<T> key, String title, float priority,
		Consumer<T> action, Icon icon, String keyStroke);

	JFrame dialogParent();
}
