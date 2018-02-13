package net.imglib2.labkit.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

public class SegmentationPanel {

	private final SegmentationResultsModel model;
	private final ComponentList< String, JPanel > list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;

	public SegmentationPanel(
			final JFrame dialogParent,
			final SegmentationResultsModel model,
			final boolean fixedLabels ) {
		this.model = model;
		this.dialogParent = dialogParent;
		this.panel = initPanel( fixedLabels );
		model.segmentationChangedListeners().add( this::update );
		update();
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void update() {
		list.clear();
		final List< String > labels = model.labels();
		final List< ARGBType > colors = model.colors();
		if ( labels == null ) return;
		for ( int i = 0; i < labels.size(); i++ ) {
			list.add( labels.get( i ), new EntryPanel( labels.get( i ), colors.get( i ) ) );
		}
		list.setSelected( model.selected() );
	}

	private JPanel initPanel( final boolean fixedLabels ) {
		final JPanel panel = new JPanel();
		panel.setPreferredSize( new Dimension( 200, 100 ) );
		panel.setLayout( new MigLayout( "insets 0, gap 4pt", "[grow]", "[][grow][][][]" ) );
		panel.add( new JLabel( "Result:" ), "wrap" );
		list.listeners().add( this::changeSelectedLabel );
		panel.add( list.getCompnent(), "grow, wrap" );
		return panel;
	}

	private void changeSelectedLabel() {
		final String label = list.getSelected();
		if ( label != null )
			model.setSelected( label );
	}

	private void changeColor( final String label ) {
		final int index = model.labels().indexOf( label );
		final ARGBType color = model.colors().get( index );
		final Color newColor = JColorChooser.showDialog(
				dialogParent,
				"Choose Color for Label \"" + label + "\"",
				new Color( color.get() ) );
		if ( newColor == null ) return;
		model.setColor( label, new ARGBType( newColor.getRGB() ) );
	}

	// -- Helper methods --

	private class EntryPanel extends JPanel {

		EntryPanel( final String value, final ARGBType color ) {
			setOpaque( true );
			setLayout( new MigLayout( "insets 4pt, gap 4pt" ) );
			final JButton comp = new JButton();
			comp.setBorder( new EmptyBorder( 1, 1, 1, 1 ) );
			comp.setIcon( createIcon( new Color( color.get() ) ) );
			comp.addActionListener( l -> changeColor( value ) );
			add( comp );
			add( new JLabel( value ) );
		}

		private ImageIcon createIcon( final Color color ) {
			final BufferedImage image =
					new BufferedImage( 20, 10, BufferedImage.TYPE_INT_RGB );
			final Graphics g = image.getGraphics();
			g.setColor( color );
			g.fillRect( 0, 0, image.getWidth(), image.getHeight() );
			g.dispose();
			return new ImageIcon( image );
		}
	}
}
