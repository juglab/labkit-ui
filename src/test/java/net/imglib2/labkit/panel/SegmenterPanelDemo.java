
package net.imglib2.labkit.panel;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmenterListModel;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.function.Supplier;

public class SegmenterPanelDemo {

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(400, 400);
		SegmenterListModel<? extends SegmentationItem> model =
			new DefaultSegmentationModel(new DatasetInputImage(ArrayImgs
				.unsignedBytes(10, 10)), new Context());
		final SegmenterPanel segmenterPanel = new SegmenterPanel(model,
			item -> newMenu(item, model));
		frame.add(segmenterPanel.getComponent());
		frame.setVisible(true);
	}

	private static JPopupMenu newMenu(Supplier<SegmentationItem> item,
		SegmenterListModel model)
	{
		JPopupMenu menu = new JPopupMenu();
		menu.add(new JMenuItem(new RunnableAction("remove", () -> model.remove(item
			.get()))));
		return menu;
	}

}
