package net.imglib2.labkit.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.imglib2.labkit.models.ColoredLabel;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

import org.scijava.ui.behaviour.util.RunnableAction;

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
		List< ColoredLabel > items = model.items();
		items.forEach( ( label ) -> list.add( label.name, new EntryPanel( label.name, label.color )) );
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
		ARGBType color = model.getColor( label );
		Color newColor = JColorChooser.showDialog(dialogParent, "Choose Color for Label \"" + label + "\"", new Color(color.get()));
		if(newColor == null) return;
		model.setColor( label, new ARGBType( newColor.getRGB() ) );
	}

	private void localize(String label) {
		model.localizeLabel( label );
	}

	// -- Helper methods --
	private class EntryPanel extends JPanel {

		EntryPanel( String value, ARGBType color ) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add( initColorButton( value, color ) );
			add(new JLabel(value), "push");
			add( initFinderButton( value ) );
		}

		private JButton initColorButton( String value, ARGBType color )
		{
			JButton colorButton = new JButton();
			colorButton.setBorder(new EmptyBorder(1,1,1,1));
			colorButton.setIcon(createIcon(new Color(color.get())));
			colorButton.addActionListener(l -> changeColor(value));
			return colorButton;
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

		private JButton initFinderButton( String value )
		{
			JButton finder = new JButton();
			finder.setBorder( BorderFactory.createEmptyBorder() );
			finder.setContentAreaFilled(false);
			finder.setOpaque(false);
			finder.setIcon(new ImageIcon(getClass().getResource( "/images/crosshair.png" )));
			finder.addActionListener(l -> localize(value));
			return finder;
		}

	}
}
