package net.imglib2.labkit;

import bdv.util.BdvSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.labkit.labeling.BdvLayer;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.Holder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import java.awt.*;

public interface Extensible {
	Context context();

	void addAction(String title, String command, Runnable action, String keyStroke);

	void addAction(AbstractNamedAction action);

	< T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
			RandomAccessibleInterval<T> img);

	Object viewerSync();

	Component dialogParent();

	Holder<Labeling> labeling();

	void setViewerTransformation(AffineTransform3D affineTransform3D);
}
