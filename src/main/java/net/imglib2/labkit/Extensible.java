
package net.imglib2.labkit;

import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;

import javax.swing.*;

public interface Extensible {

	Context context();

	void addAction(String title, String command, Runnable action,
		String keyStroke);

	JFrame dialogParent();

	ProgressConsumer progressConsumer();
}
