package net.imglib2.atlas.actions;

import bdv.viewer.ViewerPanel;
import net.imglib2.atlas.MainFrame;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class OrthogonalView {

	public OrthogonalView(MainFrame.Extensible extensible, AffineTransform3D transformation) {
		extensible.addAction(new RunnableAction("Orthogonal View", () -> {
			ViewerPanel p = (ViewerPanel) extensible.viewerSync();
			p.setCurrentViewerTransform(transformation);
		}), "");
	}
}
