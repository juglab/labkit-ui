
package sc.fiji.labkit.ui.panel;

import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.function.Supplier;

public class SegmenterPanelDemo {

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(400, 400);
		SegmenterListModel model =
			new DefaultSegmentationModel(new Context(), new DatasetInputImage(ArrayImgs
				.unsignedBytes(10, 10))).segmenterList();
		final SegmenterPanel segmenterPanel = new SegmenterPanel(model, item -> newMenu(item, model));
		frame.add(segmenterPanel);
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
