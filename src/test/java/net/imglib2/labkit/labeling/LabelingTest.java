
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
		Labeling labeling = new Labeling(Collections.emptyList(), interval);
		String label = "foobar";
		labeling.addLabel(label);
		assertTrue(labeling.getLabels().contains(label));
	}

	@Test
	public void testRemoveLabel() {
		Labeling labeling = new Labeling(Arrays.asList("f", "b"), interval);
		long[] position = { 0, 0 };
		addPixelLabel(labeling, "f", position);
		addPixelLabel(labeling, "b", position);
		labeling.removeLabel("b");
		// test
		assertFalse(labeling.getLabels().contains("b"));
		assertEquals(Collections.singletonList("f"), new ArrayList<>(getPixelLabels(
			labeling, position)));
	}

	@Test
	public void testRenameLabel() {
		// setup
		Labeling labeling = new Labeling(Arrays.asList("foreground", "background"),
			interval);
		addPixelLabel(labeling, "foreground", 0, 0);
		addPixelLabel(labeling, "background", 1, 1);
		// process
		labeling.renameLabel("foreground", "fg");
		// test
		assertEquals(Collections.singleton("fg"), getPixelLabels(labeling, 0, 0));
		assertEquals(Collections.singleton("background"), getPixelLabels(labeling,
			1, 1));
	}

	private void addPixelLabel(Labeling labeling, String value,
		long... position)
	{
		RandomAccess<? extends Set<String>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		randomAccess.get().add(value);
	}

	private Set<String> getPixelLabels(Labeling labeling, long... position) {
		RandomAccess<? extends Set<String>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		return randomAccess.get();
	}

	@Test
	public void testRenameLabel2() {
		// setup
		Labeling labeling = new Labeling(Arrays.asList("foreground", "background"),
			interval);
		// process
		labeling.renameLabel("foreground", "fg");
		// test
		assertEquals(Arrays.asList("fg", "background"), labeling.getLabels());
	}

}
