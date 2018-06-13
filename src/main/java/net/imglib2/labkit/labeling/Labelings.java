
package net.imglib2.labkit.labeling;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.view.Views;

import java.util.Collections;
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
}
