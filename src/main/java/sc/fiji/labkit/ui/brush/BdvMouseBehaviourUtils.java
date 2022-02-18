
package sc.fiji.labkit.ui.brush;

import bdv.util.BdvHandle;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

public class BdvMouseBehaviourUtils {

	private static final String ID = "MOUSE_BEHAVIOUR_BUTTON_1";

	private BdvMouseBehaviourUtils() {
		// prevent from instantiation
	}

	public static void setMouseBehaviourActive(BdvHandle bdv, Behaviour behaviour, boolean active) {
		TriggerBehaviourBindings bindings = bdv.getTriggerbindings();
		if (active) {
			final BehaviourMap behaviourMap = new BehaviourMap();
			behaviourMap.put("mouse behaviour button1", behaviour);
			behaviourMap.put("drag rotate", new Behaviour() {});
			behaviourMap.put("2d drag rotate", new Behaviour() {});
			bindings.addBehaviourMap(ID, behaviourMap);
			final InputTriggerMap inputTriggerMap = new InputTriggerMap();
			inputTriggerMap.put(InputTrigger.getFromString("button1"), "mouse behaviour button1");
			bindings.addInputTriggerMap(ID, inputTriggerMap);
		}
		else {
			bindings.removeBehaviourMap(ID);
			bindings.removeInputTriggerMap(ID);
		}
	}
}
