package net.imglib2.labkit.panel;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.imglib2.labkit.ActionsAndBehaviours;
import net.miginfocom.swing.MigLayout;

import org.scijava.ui.behaviour.DragBehaviour;

public class LabelActionsPanel extends JPanel {

	private final ButtonGroup toggles = new ButtonGroup();
	private Robot robot;
	private final List< Integer > keys;

	public LabelActionsPanel( final ActionsAndBehaviours behaviours ) {
		//TODO this panel theoretically works but the robot is of course a very bad idea.
		// -> find other ways to trigger BDV behaviours
		initRobot();
		keys = new ArrayList<>();
		setLayout( new MigLayout( "insets 0, gap 0" ) );
		addDragBehaviourToggle( "paint", KeyEvent.VK_D, "Draw (D)", behaviours );
		addDragBehaviourToggle( "fill", KeyEvent.VK_F, "Flood Fill (F)", behaviours );
		addDragBehaviourToggle( "erase", KeyEvent.VK_E, "Erase (E)", behaviours );
		addDragBehaviourToggle( "clear", KeyEvent.VK_R, "Flood Remove (R)", behaviours );
		add( initHelpButton(
				"Next Label (N)",
				"To switch between labels: Press N on the keyboard,\n" + "or select the label on the left panel." ) );
	}

	private void initRobot() {
		try {
			robot = new Robot();
		} catch ( final AWTException exc ) {
			exc.printStackTrace();
		}
	}

	private void clearRobot() {
		for ( final Integer key : keys ) {
			robot.keyRelease( key );
		}
	}

	private void addDragBehaviourToggle(
			final String title,
			final int key,
			final String tooltip,
			final ActionsAndBehaviours behaviours ) {
		keys.add( key );
		final JToggleButton btn = new JCheckBox( title );
		btn.setToolTipText( tooltip );
		toggles.add( btn );
		final Object keyCodeObject = new Integer( key );
		final String keyString = KeyEvent.getKeyText( ( Integer ) keyCodeObject );
		final KeySimulator keySimulator = new KeySimulator( key );
		behaviours.addBehaviour( new DragBehaviour() {

			@Override
			public void init( final int x, final int y ) {
				System.out.println( "init " + tooltip );
				btn.setSelected( true );
			}

			@Override
			public void end( final int x, final int y ) {
				System.out.println( "end " + tooltip );
				toggles.clearSelection();
				clearRobot();
			}

			@Override
			public void drag( final int x, final int y ) {}

		}, "set " + keyString + " btn", keyString );
		btn.addActionListener( keySimulator );
		add( btn );
	}

	private class KeySimulator implements ActionListener {

		int key;

		public KeySimulator( final int key ) {
			this.key = key;
		}

		@Override
		public void actionPerformed( final ActionEvent actionEvent ) {
			final AbstractButton abstractButton = ( AbstractButton ) actionEvent.getSource();
			final boolean selected = abstractButton.getModel().isSelected();
			if ( selected ) {
				System.out.println( "selected " + key );
				clearRobot();
				robot.keyPress( key );
			} else {
				System.out.println( "released " + key );
				robot.keyRelease( key );
			}
		}

	}

	private JButton initHelpButton( final String buttonTitle, final String helpText ) {
		final JButton button = new JButton( buttonTitle );
		button.addActionListener( l -> showMessage( buttonTitle, helpText ) );
		button.setFocusable( false );
		return button;
	}

	private void showMessage( final String text, final String explain ) {
		JOptionPane.showMessageDialog( null, explain, text, JOptionPane.INFORMATION_MESSAGE );
	}
}
