
package net.imglib2.labkit.test;

import net.imglib2.Interval;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SparseRandomAccessIntTypeTest {

	private Interval interval = Intervals.createMinSize(3, -5, 6, 8, 4, 2);

	@Test
	public void testNoEntryValue() {
		// setup
		int noEntryValue = -1;
		// process
		SparseRandomAccessIntType image = new SparseRandomAccessIntType(interval,
			noEntryValue);
		Views.iterable(image).forEach(x -> x.setInteger(noEntryValue));
		// test
		assertFalse(image.sparsityPattern().cursor().hasNext());
	}
}
