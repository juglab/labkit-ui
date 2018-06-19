
package net.imglib2.labkit.control.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.Type;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class FloodFillController {

	private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private final boolean sliceTime;

	private final FloodFillClick floodEraseBehaviour = new FloodFillClick(
		Collections::emptySet);

	private final FloodFillClick floodFillBehaviour = new FloodFillClick(
		this::selectedLabel);

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

	private Set<String> selectedLabel() {
		return Collections.singleton(model.selectedLabel().get());
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

		private final Supplier<Set<String>> value;

		FloodFillClick(Supplier<Set<String>> value) {
			this.value = value;
		}

		protected void floodFill(final RealLocalizable coords) {
			synchronized (viewer) {
				RandomAccessibleInterval<Set<String>> labeling1 = labeling();
				Point seed = roundAndReduceDimension(coords, labeling1.numDimensions());
				floodFillSet(labeling1, seed, value.get());
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

	public static void floodFillSet(RandomAccessibleInterval<Set<String>> image,
		Point seed, Set<String> label)
	{
		RandomAccessibleInterval<LabelingType<String>> labeling = RevampUtils
			.uncheckedCast(image);
		LabelingType<String> newValue = Util.getTypeFromInterval(labeling)
			.createVariable();
		newValue.clear();
		newValue.addAll(label);
		doFloodFill(labeling, seed, newValue);
	}

	public static <T extends Type<T>> void doFloodFill(
		RandomAccessibleInterval<T> image, Localizable seed, T value)
	{
		RandomAccess<T> ra = image.randomAccess();
		ra.setPosition(seed);
		T seedValue = ra.get().copy();
		if (seedValue.valueEquals(value)) return;
		BiPredicate<T, T> filter = (f, s) -> f.valueEquals(seedValue);
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target =
			Views.extendValue(image, value);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, value,
			new DiamondShape(1), filter);
	}

	private RandomAccessibleInterval<Set<String>> labeling() {
		Labeling label = model.labeling().get();
		if (sliceTime) return Views.hyperSlice(label, label.numDimensions() - 1,
			viewer.getState().getCurrentTimepoint());
		return label;
	}
}
