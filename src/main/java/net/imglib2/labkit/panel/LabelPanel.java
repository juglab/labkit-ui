package net.imglib2.labkit.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.transform.integer.BoundingBox;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

import org.scijava.ui.behaviour.util.RunnableAction;

public class LabelPanel {

	private final ImageLabelingModel model;
	private ComponentList<String, JPanel> list = new ComponentList<>();
	private final JPanel panel = initPanel();
	private final Extensible extensible;
	private Holder<Labeling> labeling;

	public LabelPanel(Extensible extensible, ImageLabelingModel model) {
		this.model = model;
		this.extensible = extensible;
		this.labeling = extensible.labeling();
		labeling.notifier().add(this::updateLabeling);
		updateLabeling(labeling.get());
		model.selectedLabel().notifier().add(this::viewSelectedLabel);
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void updateLabeling(Labeling labeling) {
		list.clear();
		labeling.getLabels().forEach(label -> list.add(label, new EntryPanel(label)));
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 100));
		panel.setLayout(new MigLayout("insets 0, gap 4pt","[grow]", "[][grow][][][]"));
		panel.add(new JLabel("Labels:"), "wrap");
		list.listeners().add(this::changeSelectedLabel);
		panel.add(list.getCompnent(), "grow, wrap");
		panel.add(new JButton(new RunnableAction("add", this::addLabel)), "grow, split 2");
		panel.add(new JButton(new RunnableAction("remove", () -> doForSelectedLabel(this::removeLabel))), "grow, wrap");
		panel.add(new JButton(new RunnableAction("rename", () -> doForSelectedLabel(this::renameLabel))), "grow, wrap");
		return panel;
	}

	private void viewSelectedLabel(String label) {
		list.setSelected(label);
	}

	private void changeSelectedLabel() {
		String label = getSelectedLabel();
		if(label != null)
			model.selectedLabel().set(label);
	}

	private void doForSelectedLabel(Consumer<String> action) {
		String label = getSelectedLabel();
		if(label != null) action.accept(label);
	}

	private String getSelectedLabel() {
		return list.getSelected();
	}

	private void addLabel() {
		String label = suggestName(labeling.get().getLabels());
		if(label == null)
			return;
		labeling.get().addLabel(label);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void removeLabel(String label) {
		labeling.get().removeLabel(label);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
		//TODO remove selection
	}

	private void renameLabel(String label) {
		String newLabel = JOptionPane.showInputDialog(extensible.dialogParent(), "Rename label \"" + label + "\" to:");
		if(newLabel == null)
			return;
		labeling.get().renameLabel(label, newLabel);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void changeColor(String label) {
		ColorMap colorMap = model.colorMapProvider().colorMap();
		ARGBType color = colorMap.getColor(label);
		Color newColor = JColorChooser.showDialog(extensible.dialogParent(), "Choose Color for Label \"" + label + "\"", new Color(color.get()));
		if(newColor == null) return;
		colorMap.setColor(label, new ARGBType(newColor.getRGB()));
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void localize(String label) {
		BoundingBox labelBox = labeling.get().getBoundingBox(label);
		AffineTransform3D transform = new AffineTransform3D();
		if(labelBox.numDimensions() > 0 && (labelBox.corner2[ 0 ] > 0 || labelBox.corner2[ 0 ] < 0)) {
			BoundingBox imgBox = new BoundingBox(model.image());
			int dim = Math.min( transform.numDimensions(), labelBox.numDimensions() );
			Float[] scales = new Float[dim];
			double[] translate = new double[ transform.numDimensions() ];
			for(int i = 0; i < dim; i++) {
				translate[ i ] = -labelBox.corner1[ i ];
				scales[ i ] = ( float ) imgBox.corner2[ i ] / ( float ) (labelBox.corner2[ i ] - labelBox.corner1[ i ] );
			}
			float scale = Collections.min( Arrays.asList( scales ) );
			transform.translate( translate );
			transform.scale( Collections.min( Arrays.asList( scale ) ) );
		}
		extensible.setViewerTransformation( transform );
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label))
				return label;
		}
		return null;
	}

	// -- Helper methods --

	private class EntryPanel extends JPanel {

		EntryPanel(String value) {
			ARGBType color = model.colorMapProvider().colorMap().getColor(value);
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			JButton comp = new JButton();
			comp.setBorder(new EmptyBorder(1,1,1,1));
			comp.setIcon(createIcon(new Color(color.get())));
			comp.addActionListener(l -> changeColor(value));
			JButton finder = new JButton();
//			finder.setBorderPainted(false);
			finder.setBorder( BorderFactory.createEmptyBorder() );
			finder.setContentAreaFilled(false); 
//			finder.setFocusPainted(false); 
			finder.setOpaque(false);
			finder.setIcon(new ImageIcon(getClass().getResource( "/images/crosshair.png" )));
			finder.addActionListener(l -> localize(value));
			add(comp);
			add(new JLabel(value), "push");
			add(finder);
		}

		private ImageIcon createIcon(Color color) {
			final BufferedImage image =
					new BufferedImage( 20, 10, BufferedImage.TYPE_INT_RGB);
			final Graphics g = image.getGraphics();
			g.setColor(color);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.dispose();
			return new ImageIcon(image);
		}
	}
}
