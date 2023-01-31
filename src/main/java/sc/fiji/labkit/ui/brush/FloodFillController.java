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
import bdv.viewer.ViewerPanel;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.*;
import sc.fiji.labkit.ui.ActionsAndBehaviours;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.LabelingModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class implements the flood fill tool.
 */
public class FloodFillController {

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private final BdvHandle bdv;

	private boolean overlapping = false;

	private boolean planarMode = false;

	private Collection<Label> visibleLabels() {
		return model.labeling().get().getLabels().stream().filter(Label::isVisible)
			.collect(Collectors.toList());
	}

	private final FloodFillClick floodFillBehaviour = new FloodFillClick(() -> {
		Label selected = selectedLabel();
		if (overlapping)
			return l -> l.add(selected);
		else {
			Collection<Label> visible = visibleLabels();
			return l -> {
				l.removeAll(visible);
				l.add(selected);
			};
		}
	});

	private final FloodFillClick floodEraseBehaviour = new FloodFillClick(() -> {
		if (overlapping) {
			Label selected = selectedLabel();
			return l -> l.remove(selected);
		}
		else {
			Collection<Label> visible = visibleLabels();
			return l -> l.removeAll(visible);
		}
	});

	public FloodFillController(final BdvHandle bdv,
		final LabelingModel model, final ActionsAndBehaviours behaviors)
	{
		this.bdv = bdv;
		this.viewer = bdv.getViewerPanel();
		this.model = model;

		RunnableAction nop = new RunnableAction("nop", () -> {});
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour(floodFillBehaviour, "floodfill", "F button1");
		behaviors.addBehaviour(floodEraseBehaviour, "floodclear", "R button1",
			"F button2", "F button3");
	}

	private Label selectedLabel() {
		return model.selectedLabel().get();
	}

	private RealPoint displayToImageCoordinates(final int x, final int y) {
		final RealPoint labelLocation = new RealPoint(3);
		labelLocation.setPosition(x, 0);
		labelLocation.setPosition(y, 1);
		labelLocation.setPosition(0, 2);
		viewer.displayToGlobalCoordinates(labelLocation);
		model.labelTransformation().applyInverse(labelLocation, labelLocation);
		return labelLocation;
	}

	public void setOverlapping(boolean override) {
		this.overlapping = override;
	}

	public void setFloodFillActive(boolean active) {
		BdvMouseBehaviourUtils.setMouseBehaviourActive(bdv, floodFillBehaviour, active);
	}

	public void setRemoveBlobActive(boolean active) {
		BdvMouseBehaviourUtils.setMouseBehaviourActive(bdv, floodEraseBehaviour, active);
	}

	public void setPlanarMode(boolean planarMode) {
		this.planarMode = planarMode;
	}

	private class FloodFillClick implements ClickBehaviour {

		private final Supplier<Consumer<Set<Label>>> operationFactory;

		FloodFillClick(Supplier<Consumer<Set<Label>>> operationFactory) {
			this.operationFactory = operationFactory;
		}

		protected void floodFill(final RealLocalizable imageCoordinates) {
			synchronized (viewer) {
				RandomAccessibleInterval<LabelingType<Label>> frame = labeling();
				if (frame.numDimensions() == 3 && planarMode) {
					long z = Math.round(imageCoordinates.getDoublePosition(2));
					frame = Views.hyperSlice(frame, 2, z);
				}
				Point seed = roundAndReduceDimension(imageCoordinates, frame.numDimensions());
				Consumer<Set<Label>> operation = operationFactory.get();
				if (askUser(frame, seed, operation))
					FloodFill.doFloodFillOnActiveLabels(frame, seed, operation);
			}
		}

		private boolean askUser(RandomAccessibleInterval<LabelingType<Label>> frame, Point seed,
			Consumer<Set<Label>> operation)
		{
			if (seed.numDimensions() == 3 && FloodFill.isBackgroundFloodFill(frame, seed, operation)) {
				String message = "Are you sure to flood fill the background of this 3d image?" +
					"\n(This may take a while to compute.)";
				int result = JOptionPane.showConfirmDialog(viewer, message, "Flood Fill 3D Image",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				return result == JOptionPane.OK_OPTION;
			}
			return true;
		}

		private Point roundAndReduceDimension(final RealLocalizable realLocalizable,
			int numDimesions)
		{
			Point point = new Point(numDimesions);
			for (int i = 0; i < point.numDimensions(); i++)
				point.setPosition(Math.round(realLocalizable.getDoublePosition(i)), i);
			return point;
		}

		@Override
		public void click(int x, int y) {
			floodFill(displayToImageCoordinates(x, y));
			model.dataChangedNotifier().notifyListeners(null);
		}
	}

	private RandomAccessibleInterval<LabelingType<Label>> labeling() {
		RandomAccessibleInterval<LabelingType<Label>> label = model.labeling()
			.get();
		if (model.isTimeSeries()) return Views.hyperSlice(label, label
			.numDimensions() - 1, viewer.state().getCurrentTimepoint());
		return label;
	}
}
