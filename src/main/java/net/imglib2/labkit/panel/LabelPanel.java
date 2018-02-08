package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class LabelPanel {

	private final ColoredLabelsModel model;
	private ComponentList<String, JPanel> list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;

	public LabelPanel( JFrame dialogParent, ColoredLabelsModel model, boolean fixedLabels ) {
		this.model = model;
		this.dialogParent = dialogParent;
		this.panel = initPanel(fixedLabels);
		model.listeners().add( this::update );
		update();
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void update() {
		list.clear();
		Map< String, ARGBType > items = model.items();
		items.forEach( ( label, color) -> list.add( label, new EntryPanel( label, color )) );
		list.setSelected( model.selected() );
	}

	private JPanel initPanel( boolean fixedLabels ) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 100));
		panel.setLayout(new MigLayout("insets 0, gap 4pt","[grow]", "[][grow][][][]"));
		panel.add(new JLabel("Labels:"), "wrap");
		list.listeners().add(this::changeSelectedLabel);
		panel.add(list.getCompnent(), "grow, wrap");
		if ( !fixedLabels ) {
			panel.add( new JButton( new RunnableAction( "add", this::addLabel ) ), "grow, split 2" );
			panel.add( new JButton( new RunnableAction( "remove", this::removeLabel ) ), "grow, wrap" );
			panel.add( new JButton( new RunnableAction( "rename", this::renameLabel ) ), "grow, wrap" );
		}
		return panel;
	}

	private void changeSelectedLabel() {
		String label = list.getSelected();
		if(label != null)
			model.setSelected( label );
	}

	private void addLabel() {
		model.addLabel();
	}

	private void removeLabel() {
		model.removeLabel( model.selected() );
	}

	private void renameLabel() {
		String label = model.selected();
		String newLabel = JOptionPane.showInputDialog(dialogParent, "Rename label \"" + label + "\" to:");
		if(newLabel == null)
			return;
		model.renameLabel( label, newLabel );
	}

	private void changeColor(String label) {
		ARGBType color = model.items().get(label);
		Color newColor = JColorChooser.showDialog(dialogParent, "Choose Color for Label \"" + label + "\"", new Color(color.get()));
		if(newColor == null) return;
		model.setColor( label, new ARGBType( newColor.getRGB() ) );
	}

	// -- Helper methods --
	private void localize(String label) {
		/*BoundingBox labelBox = labeling.get().getBoundingBox(label);
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
		labeling.notifier().forEach(l -> l.accept(labeling.get()));*/
	}

	private class EntryPanel extends JPanel {

		EntryPanel( String value, ARGBType color ) {
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
