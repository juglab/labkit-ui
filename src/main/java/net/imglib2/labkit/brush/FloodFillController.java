
package net.imglib2.labkit.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FloodFillController {

	private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private final boolean sliceTime;

	private final FloodFillClick floodEraseBehaviour = new FloodFillClick(() -> {
		Collection<Label> visible = visibleLabels();
		return l -> l.removeAll(visible);
	});

	private Collection<Label> visibleLabels() {
		return model.labeling().get().getLabels().stream().filter(Label::isActive)
			.collect(Collectors.toList());
	}

	private final FloodFillClick floodFillBehaviour = new FloodFillClick(() -> {
		Label selected = selectedLabel();
		return l -> l.add(selected);
	});

	public FloodFillController(final ViewerPanel viewer,
		final LabelingModel model, final ActionsAndBehaviours behaviors,
		final boolean sliceTime)
	{
		this.viewer = viewer;
		this.sliceTime = sliceTime;
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

	public ClickBehaviour floodEraseBehaviour() {
		return floodEraseBehaviour;
	}

	public ClickBehaviour floodFillBehaviour() {
		return floodFillBehaviour;
	}

	private RealPoint displayToImageCoordinates(final int x, final int y) {
		final RealPoint labelLocation = new RealPoint(3);
		labelLocation.setPosition(x, 0);
		labelLocation.setPosition(y, 1);
		labelLocation.setPosition(0, 2);
		viewer.displayToGlobalCoordinates(labelLocation);
		model.labelTransformation().applyInverse(labelLocation, labelLocation);
		labelLocation.move(PIXEL_CENTER_OFFSET);
		return labelLocation;
	}

	private class FloodFillClick implements ClickBehaviour {

		private final Supplier<Consumer<Set<Label>>> operationFactory;

		FloodFillClick(Supplier<Consumer<Set<Label>>> operationFactory) {
			this.operationFactory = operationFactory;
		}

		protected void floodFill(final RealLocalizable coords) {
			synchronized (viewer) {
				RandomAccessibleInterval<Set<Label>> labeling1 = labeling();
				Point seed = roundAndReduceDimension(coords, labeling1.numDimensions());
				FloodFill.doFloodFillOnActiveLabels(
					(RandomAccessibleInterval) labeling1, seed, operationFactory.get());
			}
		}

		private Point roundAndReduceDimension(final RealLocalizable realLocalizable,
			int numDimesions)
		{
			Point point = new Point(numDimesions);
			for (int i = 0; i < point.numDimensions(); i++)
				point.setPosition((long) realLocalizable.getDoublePosition(i), i);
			return point;
		}

		@Override
		public void click(int x, int y) {
			floodFill(displayToImageCoordinates(x, y));
			model.dataChangedNotifier().forEach(Runnable::run);
		}
	}

	private RandomAccessibleInterval<Set<Label>> labeling() {
		RandomAccessibleInterval<Set<Label>> label = model.labeling().get();
		if (sliceTime) return Views.hyperSlice(label, label.numDimensions() - 1,
			viewer.getState().getCurrentTimepoint());
		return label;
	}
}
