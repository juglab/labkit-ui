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

package sc.fiji.labkit.ui.models;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.panel.LabelPanel;
import sc.fiji.labkit.ui.utils.Notifier;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a list of {@link Label}, with a selected element.
 * {@link ColoredLabelsModel} is a intermediate layer between
 * {@link LabelingModel} and {@link LabelPanel}. Wraps around
 * {@link LabelingModel} and serves as a model to the {@link LabelPanel}.
 */
// TODO rename to LabelListModel
public class ColoredLabelsModel {

	private final LabelingModel model;

	private final Notifier listChangeListeners = new Notifier();
	private final Runnable onLabelingChanged = listChangeListeners::notifyListeners;

	private final Holder<Label> selected;

	public ColoredLabelsModel(LabelingModel model) {
		this.model = model;
		model.labeling().notifier().addWeakListener(onLabelingChanged);
		this.selected = new WeakListeningHolder<>(model.selectedLabel());
	}

	public List<Label> items() {
		return model.labeling().get().getLabels();
	}

	public Holder<Label> selected() {
		return selected;
	}

	public Notifier listeners() {
		return listChangeListeners;
	}

	public void addLabel() {
		Holder<Labeling> holder = model.labeling();
		Labeling labeling = holder.get();
		String newName = suggestName(labeling.getLabels().stream().map(Label::name)
			.collect(Collectors.toList()));
		if (newName == null) return;
		Label newLabel = labeling.addLabel(newName);
		model.selectedLabel().set(newLabel);
		fireLabelsChanged();
	}

	public void removeLabel(Label label) {
		model.labeling().get().removeLabel(label);
		fireLabelsChanged();
	}

	public void renameLabel(Label label, String newLabel) {
		model.labeling().get().renameLabel(label, newLabel);
		fireLabelsChanged();
	}

	public void moveLabel(Label label, int movement) {
		Labeling labeling = model.labeling().get();
		List<Label> oldOrder = new ArrayList<>(labeling.getLabels());
		Function<Label, Double> priority = l -> oldOrder.indexOf(l) + (l == label
			? movement + 0.5 * Math.signum(movement) : 0.0);
		labeling.setLabelOrder(Comparator.comparing(priority));
		fireLabelsChanged();
	}

	public void setColor(Label label, ARGBType newColor) {
		label.setColor(newColor);
		fireLabelsChanged();
	}

	private static String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label)) return label;
		}
		return null;
	}

	public void localizeLabel(final Label label) {
		Interval interval = getBoundingBox(model.labeling().get().iterableRegions()
			.get(label));
		if (interval == null) return;
		interval = Intervals.expand(interval, Math.max(interval.dimension(0), 20), 0);
		interval = Intervals.expand(interval, Math.max(interval.dimension(1), 20), 1);
		model.transformationModel().transformToShowInterval(interval, model
			.labelTransformation());
	}

	private static Interval getBoundingBox(IterableRegion<BitType> region) {
		int numDimensions = region.numDimensions();
		Cursor<?> cursor = region.cursor();
		if (!cursor.hasNext()) return null;
		long[] min = new long[numDimensions];
		long[] max = new long[numDimensions];
		cursor.fwd();
		cursor.localize(min);
		cursor.localize(max);
		while (cursor.hasNext()) {
			cursor.fwd();
			for (int i = 0; i < numDimensions; i++) {
				int pos = cursor.getIntPosition(i);
				min[i] = Math.min(min[i], pos);
				max[i] = Math.max(max[i], pos);
			}
		}
		return new FinalInterval(min, max);
	}

	public void clearLabel(Label selected) {
		model.labeling().get().clearLabel(selected);
		fireLabelsChanged();
	}

	public void setActive(Label label, boolean b) {
		label.setVisible(b);
		fireLabelsChanged();
	}

	// -- Helper methods --

	private void fireLabelsChanged() {
		Holder<Labeling> holder = model.labeling();
		holder.notifier().notifyListeners();
	}

}
