
package demo;

import bdv.util.Bdv;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.ViewerStateChange;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

public class ReplaceImageTest {

	private static final JFileChooser fileChooser = new JFileChooser();

	public static void main(String... args) {
		JFrame frame = new JFrame("Test");
		BigDataViewerComponent hp = new BigDataViewerComponent(frame);
		new ImageController(hp, "A", Color.red);
		new ImageController(hp, "B", Color.green);
		new ImageController(hp, "C", Color.blue);
		frame.add(hp);
		frame.pack();
		frame.setVisible(true);
	}

	private static class BigDataViewerComponent extends JPanel {

		private final BdvHandle handle;

		private final JPanel sidePanel;

		private BigDataViewerComponent(JFrame frame) {
			handle = new BdvHandlePanel(frame, Bdv.options());
			handle.getViewerPanel().setMinimumSize(new Dimension(500, 500));
			handle.getViewerPanel().setPreferredSize(new Dimension(500, 500));
			sidePanel = new JPanel();
			sidePanel.setLayout(new MigLayout("", "[grow]"));
			setLayout(new BorderLayout());
			add(handle.getViewerPanel());
			add(sidePanel, BorderLayout.LINE_START);
		}

		public JPanel getSidePanel() {
			return sidePanel;
		}

		public BdvHandle getHandle() {
			return handle;
		}
	}

	private static class ImageController {

		private final Random random = new Random();

		private final JButton button;

		private final JCheckBox checkBox;

		private final BigDataViewerComponent bdvComponent;

		private BdvStackSource<?> source;

		private final Color color;

		private ImageController(BigDataViewerComponent bdvComponent, String title, Color color) {
			this.color = color;
			this.bdvComponent = bdvComponent;
			addSource(bdvComponent.getHandle());
			this.button = new JButton("change " + title);
			button.addActionListener(ignore -> openImage());
			bdvComponent.getSidePanel().add(button);
			this.checkBox = new JCheckBox("show " + title);
			checkBox.addActionListener(event -> setSourceActive(checkBox.isSelected()));
			bdvComponent.getHandle().getViewerPanel().state().changeListeners().add(change -> {
				if (change == ViewerStateChange.VISIBILITY_CHANGED) {
					updateCheckbox();
				}
			});
			updateCheckbox();
			bdvComponent.getSidePanel().add(checkBox, "wrap");
		}

		private void setSourceActive(boolean selected) {
			source.setActive(selected);
		}

		private void updateCheckbox() {
			boolean visible = this.bdvComponent.getHandle().getViewerPanel().state().isSourceActive(source
				.getSources().get(0));
			checkBox.setSelected(visible);
		}

		private void addSource(BdvHandle handle) {
			if (source != null)
				source.removeFromBdv();
			ArrayImg<UnsignedByteType, ByteArray> image = ArrayImgs.unsignedBytes(100, 100, 100);
			RandomImgs.seed(random.nextInt()).randomize(image);
			source = (BdvStackSource) BdvShowable.wrap(image).show("title", BdvOptions.options().addTo(
				handle));
			source.setColor(new ARGBType(color.getRGB()));
		}

		private void openImage() {
			int returnValue = fileChooser.showOpenDialog(button);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				BdvShowable showable = openImage(file);
				replaceSource(showable);
			}
		}

		private void replaceSource(BdvShowable showable) {
			if (source != null)
				source.removeFromBdv();
			source = (BdvStackSource) showable.show("title", BdvOptions.options().addTo(bdvComponent
				.getHandle()));
			updateCheckbox();
		}

		private BdvShowable openImage(File file) {
			BdvShowable showable;
			if (file.getPath().endsWith(".xml")) {
				showable = new SpimDataInputImage(file.getAbsolutePath(), 0).showable();
			}
			else {
				ImagePlus image = new ImagePlus(file.getAbsolutePath());
				ImgPlus<?> imgPlus = VirtualStackAdapter.wrap(image);
				showable = BdvShowable.wrap((ImgPlus) imgPlus);
			}
			return showable;
		}
	}
}
