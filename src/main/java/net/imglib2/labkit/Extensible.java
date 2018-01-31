package net.imglib2.labkit;

import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.Holder;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.Context;

import java.awt.*;

public interface Extensible {
	Context context();

	void addAction(String title, String command, Runnable action, String keyStroke);

	Component dialogParent();

	Holder<Labeling> labeling();

	void setViewerTransformation(AffineTransform3D affineTransform3D);
}
