package net.imglib2.labkit.labeling;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabelingSlicerTest {

	private final Interval interval = Intervals.createMinSize(0,0,10,10);

	@Test
	public void testSingleton() {
		String labels = "xyz";
		long[] position = {4, 3};
		Labeling labeling = Labelings.singleton(interval, labels, position);
		// -- check label --
		RandomAccess<Set<String>> ra = labeling.randomAccess();
		ra.setPosition(position);
		assertTrue(ra.get().contains(labels));
		// -- count entries --
		int count = 0;
		for(Set<String> value : Views.iterable(labeling))
			count += value.size();
		assertEquals(1, count);
	}

	@Test
	public void testSlice() {
		List<RandomAccessibleInterval<Set<String>>> slices = Arrays.asList(
				Labelings.singleton(interval, "red", 5, 8),
				Labelings.singleton(interval, "green", 1, 1),
				Labelings.singleton(interval, "blue", 3, 3)
		);
		Labeling labeling = wrapLabeling(Views.stack(slices));
		List<Labeling> result = Labelings.slices(labeling);
		assertEquals(3, result.size());
		assertImageEquals(slices.get(1), result.get(1));
	}

	private void assertImageEquals(RandomAccessibleInterval<Set<String>> expected, RandomAccessibleInterval<Set<String>> actual) {
		assertTrue(Intervals.equals(expected, actual));
		Views.interval(Views.pair(expected, actual), expected).forEach(p -> assertSetEquals(p.getA(), p.getB()));
	}

	private void assertSetEquals(Set<String> actual, Set<String> expected) {
		assertEquals(actual.size(), expected.size());
		assertTrue(actual.containsAll(expected));
	}

	private Labeling wrapLabeling(RandomAccessibleInterval<Set<String>> stack) {
		List<String> labels = getLabels(stack);
		Labeling joined = new Labeling(labels, (Interval) stack);
		copy(stack, joined);
		return joined;
	}

	private void copy(RandomAccessibleInterval<Set<String>> source, RandomAccessibleInterval<Set<String>> target) {
		Views.interval(Views.pair(source, target), target).forEach(p -> {
			Set<String> b = p.getB();
			b.clear();
			p.getB().addAll(p.getA());
		});
	}

	private List<String> getLabels(RandomAccessibleInterval<Set<String>> stack) {
		Set<String> set = new TreeSet<>();
		Views.iterable(stack).forEach(set::addAll);
		return new ArrayList<>(set);
	}

}
