
package net.imglib2.labkit;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface Extensible {

	Context context();

	void addAction(String title, String command, Runnable action,
		String keyStroke);

	void addLabelMenuItem(String title, Consumer<Label> action, Icon icon);

	void addSegmenterMenuItem(String title, Consumer<SegmentationItem> action,
		Icon icon);

	JFrame dialogParent();

	ProgressConsumer progressConsumer();
}
