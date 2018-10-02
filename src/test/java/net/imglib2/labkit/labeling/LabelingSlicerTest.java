
package net.imglib2.labkit.labeling;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
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
		RandomAccess<Set<Label>> ra = labeling.randomAccess();
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
		List<RandomAccessibleInterval<Set<Label>>> slices = Arrays.asList(Labelings
			.singleton(interval, "red", 5, 8), Labelings.singleton(interval, "green",
				1, 1), Labelings.singleton(interval, "blue", 3, 3));
		Labeling labeling = wrapLabeling(Views.stack(slices));
		List<Labeling> result = Labelings.slices(labeling);
		assertEquals(3, result.size());
		assertImageEquals(slices.get(1), result.get(1));
	}

	private <T> void assertImageEquals(RandomAccessibleInterval<Set<T>> expected,
		RandomAccessibleInterval<Set<T>> actual)
	{
		assertTrue(Intervals.equals(expected, actual));
		Views.interval(Views.pair(expected, actual), expected).forEach(
			p -> assertSetEquals(p.getA(), p.getB()));
	}

	private <T> void assertSetEquals(Set<T> actual, Set<T> expected) {
		assertEquals(actual.size(), expected.size());
		assertTrue(actual.containsAll(expected));
	}

	private Labeling wrapLabeling(RandomAccessibleInterval<Set<Label>> stack) {
		List<Label> labels = getLabels(stack);
		Labeling joined = Labeling.createEmpty(labels.stream().map(Label::name)
			.collect(Collectors.toList()), stack);
		copy(stack, joined);
		return joined;
	}

	private <T> void copy(RandomAccessibleInterval<Set<T>> source,
		RandomAccessibleInterval<Set<T>> target)
	{
		Views.interval(Views.pair(source, target), target).forEach(p -> {
			Set<T> b = p.getB();
			b.clear();
			p.getB().addAll(p.getA());
		});
	}

	private <T> List<T> getLabels(RandomAccessibleInterval<Set<T>> stack) {
		Set<T> set = new HashSet<>();
		Views.iterable(stack).forEach(set::addAll);
		return new ArrayList<>(set);
	}

}
