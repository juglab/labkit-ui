package net.imglib2.labkit.panel;

import net.imglib2.labkit.models.ColoredLabel;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LabelPanel extends GroupPanel {

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
		items.forEach( ( label ) -> list.add( label.name, new EntryPanel( label.name, label.color, this )) );
		list.setSelected( model.selected() );
	}

	private JPanel initPanel( boolean fixedLabels ) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, gap 0","[grow]", "[grow][]"));
		list.listeners().add(this::changeSelectedLabel);
		list.getComponent().setBorder(BorderFactory.createEmptyBorder());
		panel.add(list.getComponent(), "grow, span, push, wrap");
		if ( !fixedLabels ) {
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBackground(UIManager.getColor("List.background"));
			buttonsPanel.setLayout(new MigLayout("insets 4pt, gap 4pt","[grow]", ""));
			buttonsPanel.add( createActionIconButton("Add label", new RunnableAction("Add label", this::addLabel), "/images/add.png"), "" );
			buttonsPanel.add( createActionIconButton("Remove all", new RunnableAction("Remove all", this::removeAllLabels), "/images/remove.png"), "gapbefore push" );
			panel.add(buttonsPanel, "grow, span");
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

	private void clearLabel() {
		model.clearLabel( model.selected() );
	}

	private void removeLabel() {
		model.removeLabel( model.selected() );
	}

	private void removeAllLabels() {
		List< ColoredLabel > items = model.items();
		items.forEach( ( label ) -> model.removeLabel(label.name));
	}

	private void renameLabel() {
		String label = model.selected();
		String newLabel = JOptionPane.showInputDialog(dialogParent, "Rename label \"" + label + "\" to:", label);
		if(newLabel == null)
			return;
		model.renameLabel( label, newLabel );
	}

	private void moveUpLabel() {
		String label = model.selected();
		model.moveLabel( label, -1 );
		update();
	}

	private void moveDownLabel() {
		String label = model.selected();
		model.moveLabel( label, 1 );
		update();
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

		EntryPanel( String value, ARGBType color, LabelPanel parent ) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add( initColorButton( value, color ) );
			add(new JLabel(value), "push");
			add( initFinderButton( value ) );
			addMouseListener(new EntryClickListener(parent));
		}

		private JButton initColorButton( String value, ARGBType color )
		{
			JButton colorButton = new JButton();
			colorButton.setBorder(new EmptyBorder(1,1,1,1));
			colorButton.setIcon(createIcon(new Color(color.get())));
			colorButton.addActionListener(l -> changeColor(value));
			return colorButton;
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


		private class EntryClickListener extends MouseAdapter {

			private LabelPanel parent;

			public EntryClickListener(LabelPanel parent) {
				this.parent = parent;
			}

			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					parent.renameLabel();
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
			public EntryOptionsMenu(LabelPanel parent) {
				add( new JMenuItem( new RunnableAction( "Rename", parent::renameLabel )));
				add( new JMenuItem( new RunnableAction( "Move up", parent::moveUpLabel )));
				add( new JMenuItem( new RunnableAction( "Move down", parent::moveDownLabel )));
				add( new JMenuItem( new RunnableAction( "Clear", parent::clearLabel )));
				add( new JMenuItem(new RunnableAction( "Remove", parent::removeLabel )));
			}
		}

	}

}
