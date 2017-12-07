package net.imglib2.atlas.labeling;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.DefaultHolder;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.LabelingComponent;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.ColorMap;
import net.imglib2.atlas.color.ColorMapProvider;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;

import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class LabelsLayer {

	private final ColorMapProvider colorProvider;

	private final Holder<Labeling> labelingHolder;

	private final RandomAccessibleContainer<ARGBType> container;

	private final LabelingComponent labelingComponent; // TODO: LabelsLayer should not depend on LabelingComponent

	private RandomAccessibleInterval<ARGBType> view;

	public LabelsLayer(Holder<Labeling> labeling, ColorMapProvider colorProvider, LabelingComponent labelingComponent) {
		this.colorProvider = colorProvider;
		this.labelingHolder = labeling;
		this.labelingComponent = labelingComponent;
		container = new RandomAccessibleContainer<>(colorView());
		view = Views.interval(container, labeling.get());
		labeling.notifier().add(this::updateLabeling);
	}

	private void updateLabeling(Labeling labeling) {
		container.setSource(colorView());
		labelingComponent.requestRepaint();
	}

	private RandomAccessibleInterval<ARGBType> colorView() {
		Labeling labeling = labelingHolder.get();
		ColorMap colorMap = colorProvider.colorMap();
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
