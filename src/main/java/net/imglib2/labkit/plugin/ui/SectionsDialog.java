package net.imglib2.labkit.plugin.ui;

import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.plugins.in.ThumbLoader;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SectionsDialog extends JDialog {

	private BufferedImageReader thumbReader;
	private List<JRadioButton> boxes;
	private List<List<Integer>> sectionIndices;
	private final ButtonGroup buttonGroup;

	private Integer selectedSection = null;

	private JOptionPane optionPane;

	public SectionsDialog(ImageReader reader, String filename) {
		setTitle("Select a section");

		setModal(true);

		buttonGroup = new ButtonGroup();

		// construct thumbnail reader
		thumbReader = new BufferedImageReader(reader);

		sectionIndices = computeSectionIndices();

		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
		/*
         * Instead of directly closing the window,
         * we're going to change the JOptionPane's
         * value property.
         */
				optionPane.setValue(new Integer(
						JOptionPane.CLOSED_OPTION));
			}
		});

		setContentPane(createOptionPane());
		JScrollPane scrollPane = new JScrollPane(createSections(filename));
		getContentPane().add(scrollPane, 0);

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

	private Container createOptionPane() {
		//Create the JOptionPane.
		optionPane = new JOptionPane("",
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION,
				null);

		optionPane.addPropertyChangeListener(e -> {
			String prop = e.getPropertyName();

			if (isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop))) {
				if (e.getNewValue().equals(JOptionPane.OK_OPTION)) {
					for (int i = 0; i < boxes.size(); i++) {
						if (boxes.get(i).isSelected()) {
							selectedSection = i;
							System.out.println("selected section: " + i);
							dispose();
							setVisible(false);
							return;
						}
					}

//          JOptionPane.showMessageDialog(this,
//                  "Please select a section",
//                  "No selection warning",
//                  JOptionPane.WARNING_MESSAGE);
				}
				if (e.getNewValue().equals(JOptionPane.CANCEL_OPTION)) {
					setVisible(false);
				}
			}
		});
		return optionPane;
	}

	//!important starts with 1
	public Integer getSelectedSection() {
		return selectedSection + 1;
	}

	public int[] getSelectedSectionIndices() {
		// TODO use list instead
		return sectionIndices.get(selectedSection).stream().mapToInt(i -> i).toArray();
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
		int sx = thumbReader.getThumbSizeX() + 10; // a little extra padding
		int sy = thumbReader.getThumbSizeY();
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
