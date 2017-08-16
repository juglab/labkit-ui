package net.imglib2.atlas.labeling;

import net.imglib2.*;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

import java.util.*;

/**
 * @author Matthias Arzt
 */
public class Labeling extends AbstractWrappedInterval {

	private final Map<String, IterableRegion<BitType>> regions = new TreeMap<>();

	public Labeling(List<String> labels, Interval source) {
		super(source);
		for(String label : labels)
			regions.put(label, new SparseRoi(source));
	}

	public int numLabels() {
		return regions().size();
	}

	public Map<String, IterableRegion<BitType>> regions() {
		return regions;
	}

	public RandomAccessibleInterval<IntType> intView() {
		List<IterableRegion<BitType>> labels = new ArrayList<>(regions.values());
		int nLabels = labels.size();
		RandomAccessibleInterval<? extends GenericComposite<BitType>> collapsed = Views.collapse(Views.stack(labels));
		Converter<GenericComposite<BitType>, IntType> converter = (in, out) -> {
			for (int i = 0; i < nLabels; i++) {
				if(in.get(i).get()) {
					out.set(i);
					return;
				}
			}
			out.set(LabelBrushController.BACKGROUND);
		};
		return Converters.convert(collapsed, converter, new IntType());
	}
}
