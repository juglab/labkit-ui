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

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelingSlicerTest {

	private final Interval interval = Intervals.createMinSize(0, 0, 10, 10);

	@Test
	public void testSingleton() {
		String labels = "xyz";
		long[] position = { 4, 3 };
		Labeling labeling = Labelings.singleton(interval, labels, position);
		// -- check label --
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(position);
		assertTrue(ra.get().stream().anyMatch(l -> l.name().equals(labels)));
		// -- count entries --
		int count = 0;
		for (Set<Label> value : Views.iterable(labeling))
			count += value.size();
		assertEquals(1, count);
	}

	@Test
	public void testSlice() {
		List<RandomAccessibleInterval<LabelingType<Label>>> slices = Arrays.asList(
			Labelings.singleton(interval, "red", 5, 8), Labelings.singleton(interval,
				"green", 1, 1), Labelings.singleton(interval, "blue", 3, 3));
		Labeling labeling = wrapLabeling(Views.stack(slices));
		List<Labeling> result = Labelings.slices(labeling);
		assertEquals(3, result.size());
		assertImageEquals(slices.get(1), result.get(1));
	}

	private <T> void assertImageEquals(
		RandomAccessibleInterval<LabelingType<T>> expected,
		RandomAccessibleInterval<LabelingType<T>> actual)
	{
		assertTrue(Intervals.equals(expected, actual));
		Views.interval(Views.pair(expected, actual), expected).forEach(
			p -> assertSetEquals(p.getA(), p.getB()));
	}

	private <T> void assertSetEquals(Set<T> actual, Set<T> expected) {
		assertEquals(actual.size(), expected.size());
		assertTrue(actual.containsAll(expected));
	}

	private Labeling wrapLabeling(
		RandomAccessibleInterval<LabelingType<Label>> stack)
	{
		List<Label> labels = getLabels(stack);
		Labeling joined = Labeling.createEmpty(labels.stream().map(Label::name)
			.collect(Collectors.toList()), stack);
		copy(stack, joined);
		return joined;
	}

	private <T> void copy(RandomAccessibleInterval<LabelingType<T>> source,
		RandomAccessibleInterval<LabelingType<T>> target)
	{
		Views.interval(Views.pair(source, target), target).forEach(p -> {
			Set<T> b = p.getB();
			b.clear();
			p.getB().addAll(p.getA());
		});
	}

	private <T> List<T> getLabels(
		RandomAccessibleInterval<LabelingType<T>> stack)
	{
		Set<T> set = new HashSet<>();
		Views.iterable(stack).forEach(set::addAll);
		return new ArrayList<>(set);
	}

}
