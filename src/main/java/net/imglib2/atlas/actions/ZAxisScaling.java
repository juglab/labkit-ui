package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class ZAxisScaling {

	public ZAxisScaling(MainFrame.Extensible extensible, AffineTransform3D sourceTransformation) {
		extensible.addAction(new RunnableAction("Change Z-Axis Scaling", () -> {
			String input = JOptionPane.showInputDialog("Scaling of z-Axis", Double.toString(sourceTransformation.get(2,2)));
			sourceTransformation.set(Double.parseDouble(input), 2, 2);
			extensible.repaint();
		}), "");
	}
}
