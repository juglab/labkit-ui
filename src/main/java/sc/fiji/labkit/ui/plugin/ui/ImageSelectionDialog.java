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

package sc.fiji.labkit.ui.plugin.ui;

import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class ImageSelectionDialog {

	public static ImageSelectionDialog show(ImageReader reader) {
		ImageSelectionDialog dialog = new ImageSelectionDialog(reader);
		int result = JOptionPane.showConfirmDialog(null, dialog.component(),
			"Select a section", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) throw new CancellationException();
		dialog.updateSelectedSection();
		return dialog;
	}

	private final String filename;
	private final BufferedImageReader thumbReader;
	private final List<List<Integer>> sectionIndices;
	private final List<JRadioButton> boxes;
	private final JComponent component;
	private int selectedSection = 0;

	private ImageSelectionDialog(ImageReader reader) {
		filename = reader.getCurrentFile();
		thumbReader = new BufferedImageReader(reader);
		sectionIndices = computeSectionIndices();
		boxes = initBoxes();
		component = new JScrollPane(createSections());
	}

	public List<Integer> getSelectedSectionIndices() {
		return sectionIndices.get(selectedSection);
	}

	public String getLabelingFilename() {
		return labelingFilename(selectedSection);
	}

	private JComponent component() {
		return component;
	}

	private void updateSelectedSection() {
		for (int i = 0; i < boxes.size(); i++)
			if (boxes.get(i).isSelected()) selectedSection = i;
	}

	private List<List<Integer>> computeSectionIndices() {
		List<List<Integer>> sections = new ArrayList<>();
		List<Integer> indices = null;
		int lastWidth = -1;
		int lastHeight = -1;
		for (int i = 0; i < thumbReader.getSeriesCount(); i++) {
			thumbReader.setSeries(i);
			if (thumbReader.isThumbnailSeries()) continue;
			int width = thumbReader.getSizeX();
			int height = thumbReader.getSizeY();
			if (indices == null || Math.abs(width - lastWidth / 2) > 2 || Math.abs(
				height - lastHeight / 2) > 2)
			{
				indices = new ArrayList<>();
				sections.add(indices);
			}
			indices.add(i);
			lastWidth = width;
			lastHeight = height;
		}
		return sections;
	}

	private List<JRadioButton> initBoxes() {
		ButtonGroup buttonGroup = new ButtonGroup();
		List<JRadioButton> boxes = new ArrayList<>();
		for (int i = 0; i < sectionIndices.size(); i++) {
			JRadioButton button = new JRadioButton(String.valueOf(i + 1));
			buttonGroup.add(button);
			boxes.add(button);
		}
		return boxes;
	}

	private Panel createSections() {
		Panel panel = new Panel();
		panel.setLayout(new MigLayout("", "[]20[][]", ""));
		panel.add(new JLabel(
			"Please select a section. You can choose the resolution in the next step."),
			"grow, span,wrap");
		for (int i = 0; i < sectionIndices.size(); i++) {
			panel.add(line(), "grow, span, wrap");
			panel.add(boxes.get(i));
			panel.add(descriptiveLabel(i), "grow,push");
			panel.add(thumbPanel(i), "wrap");
		}
		return panel;
	}

	private Component line() {
		JPanel line = new JPanel();
		line.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
			Color.lightGray));
		return line;
	}

	private Component descriptiveLabel(int i) {
		JLabel label = new JLabel();
		Font font = label.getFont();
		label.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
		String labelText = "<html>";
		if (labelingExists(i)) {
			labelText += "<b>[existing labeling found]</b><br />";
		}
		labelText += "<font face=\"verdana\">";
		for (int j : sectionIndices.get(i)) {
			thumbReader.setSeries(j);
			labelText += thumbReader.getSizeX() + " x " + thumbReader.getSizeY() +
				"<br/>";
		}
		label.setText(labelText);
		return label;
	}

	private Component thumbPanel(int index) {
		try {
			int series = sectionIndices.get(index).stream().max(Integer::compareTo)
				.orElse(0);
			thumbReader.setSeries(series);
			BufferedImage image = thumbReader.openThumbImage(thumbReader.getIndex(
				thumbReader.getSizeZ() / 2, 0, thumbReader.getSizeT() / 2));
			Panel thumbPanel = new Panel();
			thumbPanel.add(new JLabel(new ImageIcon(image)));
			return thumbPanel;
		}
		catch (FormatException | IOException e) {
			return new JPanel();
		}
	}

	private boolean labelingExists(int i) {
		return new File(labelingFilename(i)).exists();
	}

	private String labelingFilename(Integer index) {
		if (filename.endsWith(".czi") && index != null) {
			return filename.substring(0, filename.length() - ".czi".length()) + "-" +
				(index + 1) + ".czi.labeling";
		}
		return filename + ".labeling";
	}
}
