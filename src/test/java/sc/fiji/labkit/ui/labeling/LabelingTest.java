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

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.labeling.LabelingType;
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
		RandomAccess<LabelingType<Label>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		randomAccess.get().add(value);
	}

	private Set<Label> getPixelLabels(Labeling labeling, long... position) {
		RandomAccess<LabelingType<Label>> randomAccess = labeling.randomAccess();
		randomAccess.setPosition(position);
		return randomAccess.get();
	}
}
