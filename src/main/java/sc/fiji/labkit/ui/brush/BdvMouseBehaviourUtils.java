/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
