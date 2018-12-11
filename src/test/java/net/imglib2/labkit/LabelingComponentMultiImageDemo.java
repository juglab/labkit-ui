
package net.imglib2.labkit;

import bdv.util.BdvStackSource;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LabelingComponentMultiImageDemo {

	JFrame frame = initFrame();
	LabelingComponent bdvHandle = initBdvHandlePanel();
	List<BdvStackSource<?>> sources = new ArrayList<>();

	private List<String> list = Arrays.asList(
		"https://imagej.nih.gov/ij/images/boats.gif",
		"https://imagej.nih.gov/ij/images/blobs.gif",
		"https://imagej.nih.gov/ij/images/bridge.gif");

	public static void main(String... args) {
		new LabelingComponentMultiImageDemo().show();
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("Multiple Images Labeling Component Demo");
		frame.setSize(500, 400);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		return frame;
	}

	private LabelingComponent initBdvHandlePanel() {
		RandomAccessibleInterval<? extends NumericType<?>> image = ArrayImgs
			.unsignedBytes(100, 100);
		Labeling labeling = Labeling.createEmpty(Collections.emptyList(), image);
		ImageLabelingModel imageLabelingModel = new ImageLabelingModel(image,
			labeling, false);
		final LabelingComponent panel = new LabelingComponent(null,
			imageLabelingModel);
		frame.add(panel.getComponent());
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				bdvHandle.close();
			}
		});
		return panel;
	}

	private void show() {
		final JList<Entry> view = new JList<>(list.stream().map(Entry::new).toArray(
			Entry[]::new));
		view.addListSelectionListener(arg0 -> {
			if (!arg0.getValueIsAdjusting()) {
				selectImage(view.getSelectedValue());
			}
		});
		frame.add(new JScrollPane(view), BorderLayout.LINE_START);
		frame.setVisible(true);
	}

	private void selectImage(Entry entry) {
		removeOldSources();
		System.out.println(entry);
	}

	private void removeOldSources() {
		sources.forEach(BdvStackSource::removeFromBdv);
		sources.clear();
	}

	private static class Entry {

		private final String filename;
		private final RandomAccessibleInterval<?> image;

		public Entry(String filename) {
			this.filename = filename;
			this.image = ImageJFunctions.wrap(new ImagePlus(filename));
		}

		@Override
		public String toString() {
			return filename;
		}

		public RandomAccessibleInterval<?> image() {
			return image;
		}
	}
}
