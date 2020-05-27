
package net.imglib2.labkit.multi_image;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.trainable_segmentation.utils.SingletonContext;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Demo {

	static {
		LegacyInjector.preinit();
	}

	private final static Context context = SingletonContext.getInstance();

	private static List<ImageItem> files = Stream.of(
		"/home/arzt/tmp/labkit-project/phase1.tif",
		"/home/arzt/tmp/labkit-project/phase2.tif",
		"/home/arzt/tmp/labkit-project/phase3.tif",
		"/home/arzt/tmp/labkit-project/phase4.tif")
			.map(ImageItem::new)
			.collect(Collectors.toList());

	public static void main(String... args) {
		JList<ImageItem> list = initList();
		JButton button = initButton(list);
		showFrame(list, button);
	}

	private static void showFrame(JList<ImageItem> list, JButton button) {
		JFrame frame = new JFrame("Labkit Project");
		frame.setLayout(new MigLayout("", "[grow]", "[grow][]"));
		frame.add(new JScrollPane(list), "grow, wrap");
		frame.add(button);
		frame.pack();
		frame.setVisible(true);
	}

	private static JList<ImageItem> initList() {
		return new JList<>(files.toArray(new ImageItem[0]));
	}

	private static JButton initButton(JList<ImageItem> comp) {
		JButton button = new JButton("edit");
		button.addActionListener(l -> {
			ImageItem selectedValue = comp.getSelectedValue();
			if(selectedValue != null)
				selectionChanged(selectedValue);
		});
		return button;
	}

	private static void selectionChanged(ImageItem item) {
		DatasetIOService datasetIOService = context.service(DatasetIOService.class);
		Dataset dataset = CheckedExceptionUtils.run(() -> datasetIOService.open(item.getImageFile()));
		DatasetInputImage inputImage = new DatasetInputImage(dataset);
		inputImage.setDefaultLabelingFilename(item.getLabelingFile());
		LabkitFrame.showForImage(context, inputImage);
	}
}
