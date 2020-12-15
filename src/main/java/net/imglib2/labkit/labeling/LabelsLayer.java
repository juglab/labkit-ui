
package net.imglib2.labkit.labeling;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.holder.DefaultHolder;
import net.imglib2.labkit.utils.holder.Holder;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.utils.ARGBVector;
import net.imglib2.labkit.utils.ParametricNotifier;
import net.imglib2.type.numeric.ARGBType;

import java.util.List;
import java.util.Set;
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
		TIntObjectMap<ARGBType> colors = new TIntObjectHashMap<>();

		return Converters.convert(labeling.getIndexImg(), (in, out) -> {
			int i = in.getInteger();
			ARGBType c = colors.get(i);
			if (c == null) {
				c = getColor(labelSets.get(i));
				synchronized (colors) {
					colors.put(i, c);
				}
			}
			out.set(c);
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
