package net.imglib2.labkit;

import bdv.util.BdvHandle;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;

/**
 * Created by arzt on 31.08.17.
 */
public class ActionsAndBehaviours {

	private final InputTriggerConfig config = new InputTriggerConfig();

	private final Actions actions = new Actions(config);

	private final Behaviours behaviors = new Behaviours(config);

	private final BdvHandle bdvHandle;

	public ActionsAndBehaviours(BdvHandle bdvHandle) {
		this.bdvHandle = bdvHandle;
	}

	public ActionMap getActions() {
		return actions.getActionMap();
	}

	public void addAction(AbstractNamedAction action) {
		KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
		actions.namedAction(action, keyStroke != null ? keyStroke.toString() : "");
		actions.install(bdvHandle.getKeybindings(), "classifier training");
	}

	public void addBehaviour(Behaviour behaviour, String name, String... defaultTriggers) {
		behaviors.behaviour(behaviour, name, defaultTriggers);
		behaviors.install(bdvHandle.getTriggerbindings(), "classifier training");
	}
}
