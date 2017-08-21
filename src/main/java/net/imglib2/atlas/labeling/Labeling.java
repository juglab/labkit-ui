package net.imglib2.atlas.labeling;

import com.google.gson.annotations.JsonAdapter;
import net.imglib2.*;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;

import java.util.*;

/**
 * @author Matthias Arzt
 */
@JsonAdapter(LabelingSerializer.Adapter.class)
public class Labeling extends AbstractWrappedInterval {

	private Map<String, IterableRegion<BitType>> regions = Collections.emptyMap();

	public Labeling(Map<String,IterableRegion<BitType>> regions, Interval interval) {
		super(interval);
		this.regions = Collections.unmodifiableMap(new TreeMap<>(regions));
		checkIntervals();
	}

	private void checkIntervals() {
		boolean allEqual = regions.values().stream().allMatch(x -> Intervals.equals(this, x));
		if(! allEqual)
			throw new IllegalArgumentException();
	}

	public Labeling(List<String> labels, Interval interval) {
		this(initRegions(labels, interval), interval);
	}

	private static Map<String, IterableRegion<BitType>> initRegions(List<String> labels, Interval interval) {
		Map<String, IterableRegion<BitType>> regions = new TreeMap<>();
		for(String label : labels)
			regions.put(label, new SparseRoi(interval));
		return regions;
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
