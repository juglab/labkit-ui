
package net.imglib2.labkit.v2.image;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import java.awt.*;

public class View extends JPanel {

	public View(JFrame dialogParent) {
		setLayout(new BorderLayout());
		JPanel newLeftComponent = new LabelsView(dialogParent, new LabelsModel(),
			new LabelsViewListener()
			{

				@Override
				public void setActiveLabel(Label activeLabel) {

				}

				@Override
				public void addLabel() {

				}

				@Override
				public void removeAllLabels() {

				}

				@Override
				public void renameLabel(Label label, String newName) {

				}

				@Override
				public void setColor(Label label, ARGBType a) {

				}

				@Override
				public void focusLabel(Label label) {

				}

				@Override
				public void setLabelVisibility(Label label, boolean visible) {

				}

				@Override
				public JPopupMenu getPopupMenu(Label label) {
					return null;
				}
			});
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, newLeftComponent, new JPanel()));
	}

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.add(new View(frame));
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}
