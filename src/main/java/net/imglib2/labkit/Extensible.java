package net.imglib2.labkit;

import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.Context;

import java.awt.*;

public interface Extensible {
	Context context();

	void addAction(String title, String command, Runnable action, String keyStroke);

	Component dialogParent();

	void setViewerTransformation(AffineTransform3D affineTransform3D);
}
