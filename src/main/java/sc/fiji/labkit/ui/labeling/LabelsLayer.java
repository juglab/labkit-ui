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

package sc.fiji.labkit.ui.labeling;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.ui.bdv.BdvLayer;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.models.DefaultHolder;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.LabelingModel;
import sc.fiji.labkit.ui.utils.ARGBVector;
import sc.fiji.labkit.ui.utils.ConcurrentIntFunctionCache;
import sc.fiji.labkit.ui.utils.ParametricNotifier;
import net.imglib2.type.numeric.ARGBType;

import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * A {@link BdvLayer} that shows a {@link Labeling}.
 *
 * @author Matthias Arzt
 */
public class LabelsLayer implements BdvLayer {

	private final LabelingModel model;

	private final Holder<BdvShowable> showable;

	private final ParametricNotifier<Interval> listeners =
		new ParametricNotifier<>();

	private final ARGBType BLACK = new ARGBType(0);

	public LabelsLayer(LabelingModel model) {
		this.model = model;
		this.showable = new DefaultHolder<>(BdvShowable.wrap(colorView(), model.labelTransformation()));
		model.labeling().notifier().addListener(this::updateView);
		model.dataChangedNotifier().addListener(interval -> listeners.notifyListeners(interval));
	}

	private void updateView() {
		showable.set(BdvShowable.wrap(colorView(), model.labelTransformation()));
		listeners.notifyListeners(null);
	}

	private RandomAccessibleInterval<ARGBType> colorView() {
		Labeling labeling = model.labeling().get();
		List<Set<Label>> labelSets = labeling.getLabelSets();
		IntFunction<ARGBType> getColor = i -> getColor(labelSets.get(i));
		IntFunction<ARGBType> cachedGetColor = new ConcurrentIntFunctionCache<>(getColor);
		return Converters.convert(labeling.getIndexImg(), (in, out) -> {
			out.set(cachedGetColor.apply(in.getInteger()));
		}, new ARGBType());
	}

	private ARGBType getColor(Set<Label> set) {
		List<Label> visible = set.stream().filter(Label::isVisible).collect(
			Collectors.toList());
		if (visible.isEmpty()) return BLACK;
		ARGBVector collector = new ARGBVector();
		visible.forEach(label -> collector.add(label.color()));
		collector.div(visible.size());
		return collector.get();
	}

	@Override
	public Holder<BdvShowable> image() {
		return showable;
	}

	@Override
	public ParametricNotifier<Interval> listeners() {
		return listeners;
	}

	@Override
	public Holder<Boolean> visibility() {
		return model.labelingVisibility();
	}

	@Override
	public String title() {
		return "Labeling";
	}
}
