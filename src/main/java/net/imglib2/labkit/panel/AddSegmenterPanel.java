
package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.ExtensionPoints;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPluginService;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that shows a list of available segmentation algorithms. This panel is
 * usually shown only if the {@link SegmenterPanel} is empty.
 */
public class AddSegmenterPanel extends JPanel {

	public AddSegmenterPanel(SegmenterListModel segmenterListModel) {
		setLayout(new BorderLayout());
		JPanel list = new JPanel(new MigLayout("", "[grow]"));
		list.setBackground(UIManager.getColor("List.background"));
		list.add(new JLabel("Add segmentation algorithm:"), "wrap");
		addButtons(segmenterListModel, list);
		add(list);
	}

	private void addButtons(SegmenterListModel segmenterListModel, JPanel list) {
		Context context = segmenterListModel.context();
		SegmentationPluginService pluginService = context.service(SegmentationPluginService.class);
		for (SegmentationPlugin sp : pluginService.getSegmentationPlugins()) {
			JButton button = new JButton(sp.getTitle());
			button.addActionListener(ignore -> {
				segmenterListModel.addSegmenter(sp);
			});
			list.add(button, "grow, wrap");
		}
	}

	public static void main(String... args) {
		JFrame frame = new JFrame("Select Segmentation Algorithm");
		Context context = SingletonContext.getInstance();
		SegmenterListModel slm = new SegmenterListModel(context, new ExtensionPoints());
		frame.add(new AddSegmenterPanel(slm));
		frame.setSize(300, 300);
		frame.setVisible(true);
	}
}
