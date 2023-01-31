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
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Matthias Arzt
 */
public class SparseIterableRegionTest {

	Interval interval = new FinalInterval(1000, 1000, 1000);

	long[] positionA = new long[] { 42, 42, 42 };

	long[] positionB = new long[] { 10, 10, 10 };

	@Test
	public void testRandomAccess() {
		SparseIterableRegion roi = new SparseIterableRegion(interval);
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(positionA);
		assertFalse(ra.get().get());
		ra.get().set(true);
		ra.setPosition(positionB);
		assertFalse(ra.get().get());
		ra.setPosition(positionA);
		assertTrue(ra.get().get());
	}

	@Test
	public void testSize() {
		SparseIterableRegion roi = new SparseIterableRegion(interval);
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(positionA);
		ra.get().set(true);
		assertEquals(1, roi.size());
	}

	@Test
	public void testCursor() {
		SparseIterableRegion roi = new SparseIterableRegion(interval);
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(positionA);
		ra.get().set(true);
		Cursor<Void> cursor = roi.cursor();
		assertTrue(cursor.hasNext());
		cursor.fwd();
		assertEquals(positionA[1], cursor.getLongPosition(1));
		assertFalse(cursor.hasNext());
	}
}
