package net.imglib2.atlas;

import bdv.util.BdvHandle;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arzt on 31.08.17.
 */
public class ActionsAndBehaviours {

	private final InputTriggerConfig config = new InputTriggerConfig();

	private final Actions actions = new Actions(config);

	private final Behaviours behaviors = new Behaviours(config);

	private final List<AbstractNamedAction> actionsList = new ArrayList();

	private final BdvHandle bdvHandle;

	public ActionsAndBehaviours(BdvHandle bdvHandle) {
		this.bdvHandle = bdvHandle;
	}

	public List<AbstractNamedAction> getActions() {
		return actionsList;
	}

	public void addAction(AbstractNamedAction action, String keyStroke) {
		actionsList.add(action);
		actions.namedAction(action, keyStroke);
		actions.install(bdvHandle.getKeybindings(), "classifier training");
	}

	public void addBehaviour(Behaviour behaviour, String name, String... defaultTriggers) {
		behaviors.behaviour(behaviour, name, defaultTriggers);
		behaviors.install(bdvHandle.getTriggerbindings(), "classifier training");
	}
}
