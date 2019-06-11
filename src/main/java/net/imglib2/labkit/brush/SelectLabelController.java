
package net.imglib2.labkit.brush;

import bdv.viewer.ViewerPanel;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectLabelController {

	private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

	private final ClickBehaviour behaviour = this::click;
	private final ViewerPanel viewer;
	private final ImageLabelingModel model;

	public SelectLabelController(ViewerPanel viewer, ImageLabelingModel model,
		ActionsAndBehaviours actionsAndBehaviours)
	{
		this.viewer = viewer;
		this.model = model;
		actionsAndBehaviours.addBehaviour(behaviour, "select_label",
			"shift button1");
	}

	private void click(int x, int y) {
		RealPoint globalPosition = new RealPoint(3);
		viewer.displayToGlobalCoordinates(x, y, globalPosition);
		model.labelTransformation().applyInverse(globalPosition, globalPosition);
		globalPosition.move(PIXEL_CENTER_OFFSET);
		RandomAccess<LabelingType<Label>> ra = labeling().randomAccess();
		ra.setPosition(roundAndReduceDimension(globalPosition, ra.numDimensions()));
		Optional<Label> label = nextLabel(ra.get(), model.selectedLabel().get());
		label.ifPresent(model.selectedLabel()::set);
	}

	private Optional<Label> nextLabel(LabelingType<Label> labels, Label label) {
		List<Label> visibleLabels = labels.stream().filter(Label::isVisible)
			.collect(Collectors.toList());
		visibleLabels.sort(Comparator.comparing(model.labeling().get()
			.getLabels()::indexOf));
		if (visibleLabels.contains(label)) {
			int index = visibleLabels.indexOf(label);
			return Optional.of(visibleLabels.get((index + 1) % visibleLabels.size()));
		}
		return visibleLabels.stream().findFirst();
	}

	public Behaviour behaviour() {
		return behaviour;
	}

	private RandomAccessibleInterval<LabelingType<Label>> labeling() {
		RandomAccessibleInterval<LabelingType<Label>> label = model.labeling()
			.get();
		if (model.isTimeSeries()) return Views.hyperSlice(label, label
			.numDimensions() - 1, viewer.getState().getCurrentTimepoint());
		return label;
	}

	private Point roundAndReduceDimension(final RealLocalizable realLocalizable,
		int numDimesions)
	{
		Point point = new Point(numDimesions);
		for (int i = 0; i < point.numDimensions(); i++)
			point.setPosition((long) realLocalizable.getDoublePosition(i), i);
		return point;
	}
}
