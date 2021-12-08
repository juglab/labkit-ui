
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
