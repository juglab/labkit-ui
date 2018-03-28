package net.imglib2.labkit.plugin.ui;

import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.plugins.in.ThumbLoader;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class SectionsDialog {

	public static SectionsDialog show(ImageReader reader1) {
		SectionsDialog dialog = new SectionsDialog(reader1, reader1.getCurrentFile());
		int result = JOptionPane.showConfirmDialog(null, dialog.component(), "Select a section", JOptionPane.OK_CANCEL_OPTION);
		if(result != JOptionPane.OK_OPTION)
			throw new CancellationException();
		dialog.updateSelectedSection();
		return dialog;
	}

	private BufferedImageReader thumbReader;
	private List<JRadioButton> boxes;
	private List<List<Integer>> sectionIndices;
	private final ButtonGroup buttonGroup;

	private int selectedSection = 0;

	private JComponent component;

	private SectionsDialog(ImageReader reader, String filename) {
		buttonGroup = new ButtonGroup();

		// construct thumbnail reader
		thumbReader = new BufferedImageReader(reader);

		sectionIndices = computeSectionIndices();

		component = new JScrollPane(createSections(filename));
	}

	private JComponent component() {
		return component;
	}

	private List<List<Integer>> computeSectionIndices() {
		List<List<Integer>> sections = new ArrayList<>();
		List<Integer> indices = null;
		int lastWidth = -1;
		int lastHeight = -1;
		for (int i = 0; i < thumbReader.getSeriesCount(); i++) {
			thumbReader.setSeries(i);
			if (thumbReader.isThumbnailSeries())
				continue;
			int width = thumbReader.getSizeX();
			int height = thumbReader.getSizeY();
			if (indices == null || Math.abs(width - lastWidth / 2) > 2 || Math.abs(height - lastHeight / 2) > 2) {
				indices = new ArrayList<>();
				sections.add(indices);
			}
			indices.add(i);
			lastWidth = width;
			lastHeight = height;
		}
		return sections;
	}

	private void updateSelectedSection() {
		for (int i = 0; i < boxes.size(); i++) {
			if (boxes.get(i).isSelected()) {
				selectedSection = i;
			}
		}
	}

	public Integer getSelectedSection() {
		return selectedSection;
	}

	public List<Integer> getSelectedSectionIndices() {
		return sectionIndices.get(selectedSection);
	}

	public Panel createSections(String filename) {

		Panel panel = new Panel();
		panel.setLayout(new MigLayout("", "[]20[][]", ""));

		panel.add(new JLabel("Please select a section. You can choose the resolution in the next step."), "grow, span,wrap");

		boxes = new ArrayList<>();

		for (int i = 0; i < sectionIndices.size(); i++) {
			panel.add(line(), "grow, span, wrap");
			panel.add(sectionRadioButton(i));
			panel.add(descriptiveLabel(filename, i), "grow,push");
			panel.add(thumbPanel(thumbnailIndex(i)), "wrap");
		}

		return panel;

	}

	private int thumbnailIndex(int i) {
		return sectionIndices.get(i).stream().max(Integer::compareTo).orElse(0);
	}

	private Component thumbPanel(int index) {
		Panel thumbPanel = new Panel();
		ThumbLoader.loadThumb(thumbReader, index, thumbPanel, false);
		return thumbPanel;
	}

	private Component descriptiveLabel(String filename, int i) {
		JLabel label = new JLabel();
		Font font = label.getFont();
		label.setFont(font.deriveFont(font.getStyle() & ~Font.BOLD));
		String labelText = "<html>";
		if (labelingExists(filename, i)) {
			labelText += "<b>[existing labeling found]</b><br />";
		}
		labelText += "<font face=\"verdana\">";
		for (int j : sectionIndices.get(i)) {
			thumbReader.setSeries(j);
			labelText += thumbReader.getSizeX() + " x " + thumbReader.getSizeY() + "<br/>";
		}
		label.setText(labelText);
		return label;
	}

	private Component sectionRadioButton(int i) {
		JRadioButton button = new JRadioButton(String.valueOf(i + 1));
		buttonGroup.add(button);
		boxes.add(button);
		return button;
	}

	private boolean labelingExists(String filename, int i) {
		return new File(filename.replace(".czi", "") + "-" + (i + 1) + ".czi.labeling").exists();
	}

	private Component line() {
		JPanel line = new JPanel();
		line.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray));
		return line;
	}
}
