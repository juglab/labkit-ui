
package net.imglib2.labkit.labeling;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Labelings {

	public static List<Labeling> slices(Labeling labeling) {
		int sliceDimension = labeling.numDimensions() - 1;
		Interval sliceInterval = RevampUtils.removeLastDimension(labeling);
		List<String> labels = labeling.getLabels();
		List<Labeling> slices = IntStream.range(0, Math.toIntExact(labeling
			.dimension(sliceDimension))).mapToObj(ignore -> new Labeling(labels,
				sliceInterval)).collect(Collectors.toList());
		sparseCopy(labeling, Views.stack(slices));
		return slices;
	}

	private static void sparseCopy(Labeling labeling,
		RandomAccessibleInterval<Set<String>> target)
	{
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<Set<String>> out = target.randomAccess();
		RandomAccess<Set<String>> in = labeling.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			in.setPosition(cursor);
			out.setPosition(cursor);
			copy(in.get(), out.get());
		}
	}

	private static void copy(Set<String> in, Set<String> out) {
		out.clear();
		out.addAll(in);
	}

	public static Labeling singleton(Interval interval, String label,
		long... coordinates)
	{
		Labeling labeling = new Labeling(Collections.singletonList(label),
			interval);
		RandomAccess<Set<String>> ra = labeling.randomAccess();
		ra.setPosition(coordinates);
		ra.get().add(label);
		return labeling;
	}

	public static Labeling of(ImgLabeling<String, ?> input) {
		return new Labeling(toSortedList(getExistingLabels(input)), input);
	}

	private static List< String > toSortedList(Set< String > labels) {
		List<String> labelsList = new ArrayList<>(labels);
		labelsList.sort(String::compareTo);
		return labelsList;
	}

	private static Set< String > getExistingLabels(
			ImgLabeling< String, ? > input)
	{
		HashSet< IntegerType< ? > > values = existingValues(
				(RandomAccessibleInterval) input.getIndexImg());
		LabelingMapping< String > mapping = input.getMapping();
		Set<String> labels = new HashSet<>();
		for (IntegerType< ? > index : values)
			labels.addAll(mapping.labelsAtIndex(index.getInteger()));
		return labels;
	}

	private static <T extends IntegerType<T>> HashSet< T > existingValues(
			RandomAccessibleInterval< T > indexImg)
	{
		HashSet< T > values = new HashSet<>();
		for (T value : Views.iterable(indexImg))
			if (!values.contains(value)) values.add(value.copy());
		return values;
	}
}
