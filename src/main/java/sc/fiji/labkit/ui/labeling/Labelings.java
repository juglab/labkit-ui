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

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import sc.fiji.labkit.ui.utils.DimensionUtils;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility functions associated with {@link Labeling}.
 */
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
		RandomAccessibleInterval<LabelingType<Label>> target)
	{
		Cursor<?> cursor = labeling.sparsityCursor();
		RandomAccess<LabelingType<Label>> out = target.randomAccess();
		RandomAccess<LabelingType<Label>> in = labeling.randomAccess();
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
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
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
		return ImgLabeling.fromImageAndLabelSets(Cast.unchecked(input.getIndexImg()), newLabels);
	}
}
