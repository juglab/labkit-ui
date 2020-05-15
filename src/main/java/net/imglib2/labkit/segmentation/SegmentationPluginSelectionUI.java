
package net.imglib2.labkit.segmentation;

import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;

public class SegmentationPluginSelectionUI {

	public static void main(String... args) {
		JButton button = new JButton("Click me");
		button.addActionListener(ignore -> show(button));
		JFrame frame = new JFrame("Hello World");
		frame.add(button);
		frame.pack();
		frame.setVisible(true);
	}

	public static Segmenter show(JButton button) {
		Context context = new Context();
		JPanel content = new JPanel(new BorderLayout());
		JPanel list = new JPanel(new MigLayout("", "[grow]"));
		SegmentationPluginService pluginService = context.service(SegmentationPluginService.class);
		String title = "Select Segmentation Algorithm";
		for (SegmentationPlugin sp : pluginService.getSegmentationPlugins()) {
			list.add(new JButton(sp.getTitle()), "grow, wrap");
		}
		content.add(new JScrollPane(list));
		JOptionPane.showOptionDialog(null, content, title, JOptionPane.DEFAULT_OPTION,
			JOptionPane.PLAIN_MESSAGE, null, new Object[] { "Cancel" }, "Cancel");
		return null;
	}
}
