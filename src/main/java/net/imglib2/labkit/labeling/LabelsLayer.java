package net.imglib2.labkit.labeling;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.RandomAccessibleContainer;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class LabelsLayer {

	private final RandomAccessibleContainer<ARGBType> container;

	private final LabelingModel model;

	private RandomAccessibleInterval<ARGBType> view;

	public LabelsLayer(LabelingModel model) {
		this.model = model;
		RandomAccessibleInterval<ARGBType> view = colorView();
		container = new RandomAccessibleContainer<>(view);
		this.view = Views.interval(container, view);
		model.labeling().notifier().add(this::updateLabeling);
	}

	private void updateLabeling(Labeling labeling) {
		container.setSource(colorView());
		model.requestRepaint();
	}

	private RandomAccessibleInterval<ARGBType> colorView() {
		Labeling labeling = model.labeling().get();
		ColorMap colorMap = model.colorMapProvider().colorMap();
		TIntObjectMap<ARGBType> colors = new TIntObjectHashMap<>();

		return Converters.convert(labeling.getIndexImg(), (in, out) -> {
			int i = in.getInteger();
			ARGBType c = colors.get(i);
			if(c == null) {
				Set<String> set = labeling.getLabelSets().get(i);
				c = set.isEmpty() ? new ARGBType(0) : colorMap.getColor(set.iterator().next());
				colors.put(i, c);
			}
			out.set(c);
		}, new ARGBType());
	}

	public RandomAccessibleInterval<ARGBType> view() {
		return view;
	}
}
