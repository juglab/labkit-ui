
package net.imglib2.labkit.labeling;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LabelingTest {

	private final FinalInterval interval = Intervals.createMinSize(0, 0, 2, 2);

	@Test
	public void testAddLabel() {
		Labeling labeling = Labeling.createEmpty(Collections.emptyList(), interval);
		Label label = labeling.addLabel("foobar");
		assertTrue(labeling.getLabels().contains(label));
	}

	@Test
	public void testRemoveLabel() {
		Labeling labeling = Labeling.createEmpty(Arrays.asList("f", "b"), interval);
		Label f = labeling.getLabel("f");
		Label b = labeling.getLabel("b");
		long[] position = { 0, 0 };
		addPixelLabel(labeling, f, position);
		addPixelLabel(labeling, b, position);
		labeling.removeLabel(b);
		// test
		assertFalse(labeling.getLabels().contains(b));
		assertEquals(Collections.singletonList(f), new ArrayList<>(getPixelLabels(
			labeling, position)));
	}

	private void addPixelLabel(Labeling labeling, Label value, long... position) {
		RandomAccess<? extends Set<Label>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		randomAccess.get().add(value);
	}

	private Set<Label> getPixelLabels(Labeling labeling, long... position) {
		RandomAccess<? extends Set<Label>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		return randomAccess.get();
	}

	@Test
	public void testFromStrings() {
		Labeling labeling = Labeling.fromStrings(new String[] { null, "a", "b",
			null }, 2, 2);
		assertEquals(Collections.singleton(labeling.getLabel("a")), getPixelLabels(
			labeling, 1, 0));
		assertEquals(Collections.singleton(labeling.getLabel("b")), getPixelLabels(
			labeling, 0, 1));
		assertEquals(Collections.emptySet(), getPixelLabels(labeling, 1, 1));
	}
}
