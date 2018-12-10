
package net.imglib2.labkit;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabelingComponentMultiImageDemo {

	JFrame frame = initFrame();
	BdvHandlePanel bdvHandle = initBdvHandlePanel();
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

	private BdvHandlePanel initBdvHandlePanel() {
		final BdvHandlePanel panel = new BdvHandlePanel(frame, Bdv.options()
			.is2D());
		frame.add(panel.getViewerPanel());
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
		sources.add(BdvFunctions.show(entry.image(), "image", BdvOptions.options()
			.addTo(bdvHandle)));
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
