
package net.imglib2.labkit.v2.image;

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ImageView extends JPanel {

	public ImageView(JFrame dialogParent) {
		setLayout(new BorderLayout());
		JPanel left = new JPanel();
		left.setLayout(new MigLayout("", "[grow]", "[grow]"));
		LabelsView labelsView = new LabelsView(dialogParent, new LabelsModel(),
			new DummyLabelsViewListener());
		left.add(labelsView, "grow");
		JPanel right = new JPanel();
		right.setLayout(new MigLayout("", "[grow]", "[][grow]"));
		right.add(new ToolBarView(new ToolBarModel()), "grow, wrap");
		right.add(new BdvHandlePanel(dialogParent, Bdv.options()).getViewerPanel(), "grow");
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
	}

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.add(new ImageView(frame));
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}
