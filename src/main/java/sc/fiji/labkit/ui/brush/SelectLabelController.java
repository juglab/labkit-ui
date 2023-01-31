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
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.*;
import sc.fiji.labkit.ui.ActionsAndBehaviours;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements the color picker tool.
 */
public class SelectLabelController {

	private static final double[] PIXEL_CENTER_OFFSET = { 0.5, 0.5, 0.5 };

	private final ClickBehaviour behaviour = this::click;

	private final BdvHandle bdv;

	private final ViewerPanel viewer;

	private final ImageLabelingModel model;

	public SelectLabelController(BdvHandle bdv, ImageLabelingModel model,
		ActionsAndBehaviours actionsAndBehaviours)
	{
		this.bdv = bdv;
		this.viewer = bdv.getViewerPanel();
		this.model = model;
		actionsAndBehaviours.addBehaviour(behaviour, "select_label",
			"shift button1");
	}

	public void setActive(boolean active) {
		BdvMouseBehaviourUtils.setMouseBehaviourActive(bdv, behaviour, active);
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

	private RandomAccessibleInterval<LabelingType<Label>> labeling() {
		RandomAccessibleInterval<LabelingType<Label>> label = model.labeling()
			.get();
		if (model.isTimeSeries()) return Views.hyperSlice(label, label
			.numDimensions() - 1, viewer.state().getCurrentTimepoint());
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
