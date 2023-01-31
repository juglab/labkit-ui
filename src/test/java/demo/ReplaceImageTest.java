/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.SpimDataInputImage;
import sc.fiji.labkit.ui.models.DefaultHolder;
import sc.fiji.labkit.ui.models.Holder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ReplaceImageTest {

	private static final JFileChooser fileChooser = new JFileChooser();

	public static void main(String... args) {
		JFrame frame = new JFrame("Test");
		Model model = new Model();
		BigDataViewerComponent hp = new BigDataViewerComponent(frame, model);
		new ImageController(model.image(), model.imageVisibility(), hp.getSidePanel(), "image");
		new ImageController(model.labeling(), model.labelingVisibility(), hp.getSidePanel(),
			"labeling");
		new ImageController(model.segmentation(), model.segmentationVisibility(), hp.getSidePanel(),
			"segmentation");
		frame.add(hp);
		frame.pack();
		frame.setVisible(true);
	}

	private static class Model {

		private final Holder<BdvShowable> image = new DefaultHolder<>(null);

		private final Holder<Boolean> imageVisibility = new DefaultHolder<>(true);

		private final Holder<BdvShowable> labeling = new DefaultHolder<>(null);

		private final Holder<Boolean> labelingVisibility = new DefaultHolder<>(true);

		private final Holder<BdvShowable> segmentation = new DefaultHolder<>(null);

		private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(true);

		public Holder<BdvShowable> image() {
			return image;
		}

		public Holder<Boolean> imageVisibility() {
			return imageVisibility;
		}

		public Holder<BdvShowable> labeling() {
			return labeling;
		}

		public Holder<Boolean> labelingVisibility() {
			return labelingVisibility;
		}

		public Holder<BdvShowable> segmentation() {
			return segmentation;
		}

		public Holder<Boolean> segmentationVisibility() {
			return segmentationVisibility;
		}
	}

	private static class BigDataViewerComponent extends JPanel {

		private final BdvHandle handle;

		private final JPanel sidePanel;

		private BigDataViewerComponent(JFrame frame, Model model) {
			this.handle = new BdvHandlePanel(frame, Bdv.options());
			handle.getViewerPanel().setMinimumSize(new Dimension(500, 500));
			handle.getViewerPanel().setPreferredSize(new Dimension(500, 500));
			new BigDataViewEntry(handle, model.image(), model.imageVisibility());
			new BigDataViewEntry(handle, model.labeling(), model.labelingVisibility());
			new BigDataViewEntry(handle, model.segmentation(), model.segmentationVisibility());
			sidePanel = new JPanel();
			sidePanel.setLayout(new MigLayout("", "[grow]"));
			setLayout(new BorderLayout());
			add(handle.getViewerPanel());
			add(sidePanel, BorderLayout.LINE_START);
		}

		public JPanel getSidePanel() {
			return sidePanel;
		}
	}

	private static class BigDataViewEntry {

		private final BdvHandle handle;

		private final Holder<BdvShowable> image;

		private final Holder<Boolean> visibility;

		private BdvStackSource<?> source;

		public BigDataViewEntry(BdvHandle handle, Holder<BdvShowable> image,
			Holder<Boolean> visibility)
		{
			this.handle = handle;
			this.image = image;
			this.visibility = visibility;
			this.source = null;
			handle.getViewerPanel().state().changeListeners().add(this::viewerStateChanged);
			visibility.notifier().addListener(this::visibilityChanged);
			image.notifier().addListener(this::imageChanged);
			imageChanged();
		}

		private void imageChanged() {
			BdvStackSource<?> source = this.source;
			this.source = null;
			if (source != null) source.removeFromBdv();
			BdvShowable showable = image.get();
			if (showable != null)
				this.source = (BdvStackSource<?>) showable.show("title", BdvOptions.options().addTo(
					handle));
			visibilityChanged();
		}

		private void visibilityChanged() {
			BdvStackSource<?> source = this.source;
			if (source != null) {
				Boolean visible = visibility.get();
				source.setActive(visible);
			}
		}

		private void viewerStateChanged(ViewerStateChange change) {
			BdvStackSource<?> source = this.source;
			if (source != null && change == ViewerStateChange.VISIBILITY_CHANGED) {
				boolean visible = handle.getViewerPanel().state().isSourceActive(source.getSources().get(
					0));
				visibility.set(visible);
			}
		}
	}

	private static class ImageController {

		private final JButton button;

		private final JCheckBox checkBox;

		private final Holder<BdvShowable> image;

		private final Holder<Boolean> visibility;

		private ImageController(Holder<BdvShowable> image, Holder<Boolean> visibility, JPanel sidePanel,
			String title)
		{
			this.image = image;
			this.visibility = visibility;
			this.button = new JButton("change " + title);
			button.addActionListener(ignore -> openImage());
			sidePanel.add(button);
			this.checkBox = new JCheckBox("show " + title);
			checkBox.addActionListener(this::checkBoxChanged);
			visibility.notifier().addListener(this::visibilityChanged);
			visibilityChanged();
			sidePanel.add(checkBox, "wrap");
		}

		private void checkBoxChanged(ActionEvent ignore) {
			visibility.set(checkBox.isSelected());
		}

		private void visibilityChanged() {
			checkBox.setSelected(visibility.get());
		}

		private void openImage() {
			int returnValue = fileChooser.showOpenDialog(button);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				BdvShowable showable = openImage(file);
				image.set(showable);
			}
		}

		private static BdvShowable openImage(File file) {
			if (file.getPath().endsWith(".xml")) {
				return new SpimDataInputImage(file.getAbsolutePath(), 0).showable();
			}
			else {
				ImagePlus image = new ImagePlus(file.getAbsolutePath());
				ImgPlus<?> imgPlus = VirtualStackAdapter.wrap(image);
				return BdvShowable.wrap((ImgPlus) imgPlus);
			}
		}
	}
}
