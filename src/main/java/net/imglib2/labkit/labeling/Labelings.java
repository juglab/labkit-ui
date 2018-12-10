
package net.imglib2.labkit.labeling;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Labelings {

	public static List<Labeling> slices(Labeling labeling) {
		int sliceDimension = labeling.numDimensions() - 1;
		Interval sliceInterval = DimensionUtils.removeLastDimension(labeling);
		List<Label> labels = labeling.getLabels();
		List<Labeling> slices = IntStream.range(0, Math.toIntExact(labeling
			.dimension(sliceDimension))).mapToObj(ignore -> Labeling
				.createEmptyLabels(labels, sliceInterval)).collect(Collectors.toList());
		sparseCopy(labeling, Views.stack(slices));
		return slices;
	}

	private static void sparseCopy(Labeling labeling,
		RandomAccessibleInterval<Set<Label>> target)
	{
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<Set<Label>> out = target.randomAccess();
		RandomAccess<Set<Label>> in = labeling.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			in.setPosition(cursor);
			out.setPosition(cursor);
			copy(in.get(), out.get());
		}
	}

	private static <T> void copy(Set<T> in, Set<T> out) {
		out.clear();
		out.addAll(in);
	}

	public static Labeling singleton(Interval interval, String labelName,
		long... coordinates)
	{
		Labeling labeling = Labeling.createEmpty(Collections.singletonList(
			labelName), interval);
		Label label = labeling.getLabels().iterator().next();
		RandomAccess<Set<Label>> ra = labeling.randomAccess();
		ra.setPosition(coordinates);
		ra.get().add(label);
		return labeling;
	}

	static <S, T> ImgLabeling<T, ?> mapLabels(ImgLabeling<S, ?> input,
		Function<S, T> mapping)
	{
		LabelingMapping<S> oldLabels = input.getMapping();
		Map<S, T> map = oldLabels.getLabels().stream().collect(Collectors.toMap(
			s -> s, s -> mapping.apply(s)));
		List<Set<T>> newLabels = new ArrayList<>();
		for (int i = 0; i < oldLabels.numSets(); i++) {
			final Set<S> old = oldLabels.labelsAtIndex(i);
			final Set<T> newer = old.stream().map(map::get).collect(Collectors
				.toSet());
			newLabels.add(newer);
		}
		return LabelingSerializer.fromImageAndLabelSets(input.getIndexImg(),
			newLabels);
	}
}
