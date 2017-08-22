package net.imglib2.atlas.labeling;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.LabelingComponent;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.ColorMap;
import net.imglib2.atlas.color.ColorMapProvider;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		List<Map.Entry<String, IterableRegion<BitType>>> entries = new ArrayList<>(labelingHolder.get().regions().entrySet());
		ColorMap colorMap = colorProvider.colorMap();
		List<ARGBType> colors = entries.stream().map(x -> colorMap.getColor(x.getKey())).collect(Collectors.toList());
		List<IterableRegion<BitType>> regions = entries.stream().map(Map.Entry::getValue).collect(Collectors.toList());
		return colorView(regions, colors);
	}

	public static RandomAccessibleInterval<ARGBType> colorView(List<IterableRegion<BitType>> regions, List<ARGBType> colors) {
		int nLabels = regions.size();
		RandomAccessibleInterval<? extends GenericComposite<BitType>> collapsed = Views.collapse(Views.stack(regions));
		Converter<GenericComposite<BitType>, ARGBType> converter = (in, out) -> {
			for (int i = 0; i < nLabels; i++)
				if (in.get(i).get()) {
					out.set(colors.get(i));
					return;
				}
			out.set(0);
		};
		return Converters.convert(collapsed, converter, new ARGBType());
	}

	public RandomAccessibleInterval<ARGBType> view() {
		return view;
	}
}
