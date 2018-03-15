package net.imglib2.labkit.plugin.ui;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import ij.gui.GenericDialog;
import loci.formats.ImageReader;
import loci.formats.gui.BufferedImageReader;
import loci.plugins.in.ThumbLoader;
import loci.plugins.util.WindowTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SectionsDialog extends GenericDialog
        implements ActionListener {

  private BufferedImageReader thumbReader;
  private List<Panel> p;
  private List<JRadioButton> boxes;
  private List<JLabel> sizes;
  private List<Integer> sectionIndices;
  private final ButtonGroup buttonGroup;
  private ImageReader reader;

  private final int seriesCount;
  private Integer selectedSection = null;

  private Button okay;

  public SectionsDialog(ImageReader reader) {
    super("Select a section");
    this.reader = reader;

    seriesCount = reader.getSeriesCount();

    buttonGroup = new ButtonGroup();

    // construct thumbnail reader
    thumbReader = new BufferedImageReader(reader);

    int sectionCount = 0;
    p = new ArrayList<>();
    boxes = new ArrayList<>();
    sizes = new ArrayList<>();
    sectionIndices = new ArrayList<>();
    int lastWidth = -1;
    int lastHeight = -1;
    for (int i = 0; i < seriesCount; i++) {
      thumbReader.setSeries(i);
      int width = thumbReader.getSizeX();
      int height = thumbReader.getSizeX();
      if(lastWidth < 0){
        //first section
        sectionCount++;
        Panel panel = new Panel();
        p.add(panel);
        JRadioButton button = new JRadioButton(String.valueOf(sectionCount));
        boxes.add(button);
        buttonGroup.add(button);
        sizes.add(new JLabel(width + " x " + height));
        sectionIndices.add(i);
      } else {
        if(Math.abs(width - lastWidth/2) < 2 && Math.abs(height - lastHeight/2) < 2 ) {
          //same section
          JLabel label = sizes.get(sizes.size()-1);
          label.setText(label.getText() + "\n" + height + " x " + height);
        } else {
          //load last sections thumbnail (smallest one should be the last)
          Panel lastPanel = p.get(p.size()-1);
          int sx = thumbReader.getThumbSizeX() + 10; // a little extra padding
          int sy = thumbReader.getThumbSizeY();
          lastPanel.add(Box.createRigidArea(new Dimension(sx, sy)));
          ThumbLoader.loadThumb(thumbReader, i-1, lastPanel, true);
          //create new section
          sectionCount++;
          Panel panel = new Panel();
          p.add(panel);
          JRadioButton button = new JRadioButton(String.valueOf(sectionCount));
          boxes.add(button);
          buttonGroup.add(button);
          sizes.add(new JLabel(width + " x " + height));
          sectionIndices.add(i);
        }
      }
      lastWidth = width;
      lastHeight = height;
    }
    if(seriesCount > 0) {
      Panel lastPanel = p.get(p.size()-1);
      int sx = thumbReader.getThumbSizeX() + 10; // a little extra padding
      int sy = thumbReader.getThumbSizeY();
      lastPanel.add(Box.createRigidArea(new Dimension(sx, sy)));
      ThumbLoader.loadThumb(thumbReader, seriesCount-1, lastPanel, true);
    }

    addSections();

    okay = new Button("ok");
    okay.addActionListener(this);
    okay.addKeyListener(this);
    add(okay);
  }

  public Integer getSelectedSection() {
    return selectedSection;
  }

  public int[] getSelectedSectionIndices() {

    int firstIndex = sectionIndices.get(selectedSection);
    int lastIndex;
    if(sectionIndices.size() > selectedSection+1) {
      lastIndex = sectionIndices.get(selectedSection+1)-1;
    }else {
      lastIndex = seriesCount-1;
    }

    int[] res = new int[lastIndex-firstIndex+1];
    for(int i = firstIndex; i <= lastIndex; i++) {
      res[i-firstIndex] = i;
    }

    return res;

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String cmd = e.getActionCommand();
    Object source = e.getSource();
    if(source.equals(okay)){
      for(int i = 0; i < boxes.size(); i++){
        if(boxes.get(i).isSelected()){
          selectedSection = i;
          System.out.println("selected section: " + i);
          dispose();
          return;
        }
      }
    }
  }

  public void addSections() {
    // rebuild dialog to organize things more nicely

    final String cols = p == null ? "pref" : "pref, 3dlu, pref";

    final StringBuilder sb = new StringBuilder("pref");
    for (int s = 1; s < seriesCount; s++) sb.append(", 3dlu, pref");
    final String rows = sb.toString();

    final PanelBuilder builder = new PanelBuilder(new FormLayout(cols, rows));
    final CellConstraints cc = new CellConstraints();

    int row = 1;
    for (int s = 0; s < boxes.size(); s++) {
      builder.add(boxes.get(s), cc.xy(1, row));
      builder.add(sizes.get(s), cc.xy(1, row));
      if (p != null) builder.add(p.get(s), cc.xy(3, row));
      row += 2;
    }

    final JPanel masterPanel = builder.getPanel();

    removeAll();

    GridBagLayout gdl = (GridBagLayout) getLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gdl.setConstraints(masterPanel, gbc);

    add(masterPanel);

    WindowTools.addScrollBars(this);
    setBackground(Color.white); // HACK: workaround for JPanel in a Dialog

  }

}



