package net.imglib2.atlas;

import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.atlas.labeling.Labeling;
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

	void addBehaviour(Behaviour behaviour, String name, String defaultTriggers);

	void addOverlayRenderer(OverlayRenderer overlay);

	void displayRepaint();

	Labeling getLabeling();

	void setLabeling(Labeling labeling);
}
