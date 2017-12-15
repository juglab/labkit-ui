package net.imglib2.labkit;

import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.Context;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import java.awt.*;

public interface Extensible {
	Context context();

	void repaint();

	void addAction(String title, String command, Runnable action, String keyStroke);

	void addAction(AbstractNamedAction action);

	< T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
			RandomAccessibleInterval<T> img);

	Object viewerSync();

	<T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction);

	Component dialogParent();

	Holder<Labeling> labeling();
}
