package net.imglib2.labkit.panel;

import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SegmentationPanel extends GroupPanel {

	private final SegmentationResultsModel model;
	private final ComponentList< String, JPanel > list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;
	private final ActionMap actions;

	public SegmentationPanel(
			final JFrame dialogParent,
			final SegmentationResultsModel model,
			final boolean fixedLabels, SegmentationComponent segmentationComponent) {
		this.model = model;
		this.dialogParent = dialogParent;
		this.actions = segmentationComponent.getActions();
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
		//fake, replacit
		list.add("RandomForest", new SegmentationEntryPanel("RandomForest", this));
		for ( int i = 0; i < labels.size(); i++ ) {
			list.add( labels.get( i ), new EntryPanel( labels.get( i ), colors.get( i ), this ) );
		}
		list.setSelected( model.selected() );
	}

	private JPanel initPanel(final boolean fixedLabels) {
		final JPanel panel = new JPanel();
		panel.setLayout( new MigLayout( "insets 0, gap 0", "[grow]", "[grow][]" ) );
		list.listeners().add( this::changeSelectedLabel );
		list.getComponent().setBorder(BorderFactory.createEmptyBorder());
		panel.add( list.getComponent(), "wrap, grow, span" );
		if ( !fixedLabels ) {
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBackground(UIManager.getColor("List.background"));
			buttonsPanel.setLayout(new MigLayout("insets 4pt, gap 4pt","[grow]", ""));
			buttonsPanel.add( createActionIconButton("Settings", actions.get("Select Classification Algorithm ..."), "/images/gear.png"), "" );
			buttonsPanel.add( createActionIconButton("Run segmentation", actions.get( "Train Classifier" ), "/images/run.png"), "gapbefore push" );
			panel.add(buttonsPanel, "grow, span");
		}
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

	private void openSegmentationSettings() {

	}

	// -- Helper methods --

	private class SegmentationEntryPanel extends JPanel {

		SegmentationEntryPanel( final String value, SegmentationPanel parent ) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt"));
			add(new JLabel(value));
			addMouseListener(new EntryClickListener(parent));
		}
	}

	private class EntryPanel extends JPanel {

		EntryPanel( final String value, final ARGBType color, SegmentationPanel parent ) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt 8pt 4pt 4pt, gap 4pt"));
			add(new JLabel(new ImageIcon(this.getClass().getResource("/images/leaf.png"))));
			final JButton comp = new JButton();
			comp.setBorder(new EmptyBorder(1, 1, 1, 1));
			comp.setIcon(createIcon(new Color(color.get())));
			comp.addActionListener(l -> changeColor(value));
			add(comp);
			add(new JLabel(value));
			addMouseListener(new EntryClickListener(parent));
		}
	}

	private class EntryClickListener extends MouseAdapter {

		private SegmentationPanel parent;

		public EntryClickListener(SegmentationPanel parent) {
			this.parent = parent;
		}

		public void mouseClicked(MouseEvent event) {
			if (event.getClickCount() == 2) {
//				parent.renameLabel();
			}
		}

		public void mousePressed(MouseEvent e){
			if (e.isPopupTrigger())
				doPop(e);
		}

		public void mouseReleased(MouseEvent e){
			if (e.isPopupTrigger())
				doPop(e);
		}

		private void doPop(MouseEvent e){
			EntryOptionsMenu menu = new EntryOptionsMenu(parent);
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private class EntryOptionsMenu extends JPopupMenu {
		public EntryOptionsMenu(SegmentationPanel parent) {
			add( new JMenuItem( "test" ));
		}
	}
}
