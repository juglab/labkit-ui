package net.imglib2.sparse;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SparseIterableRegionTest
{
	@Test
	public void testOriginRemoveBug() {
		// NB: Test bug were pixel at position (0,0) was set to zero, whenever a random access was created.
		// setup
		long[] origin = { 0, 0 };
		SparseIterableRegion region = new SparseIterableRegion( new FinalInterval( origin, origin ) );
		Views.iterable( region ).forEach( BitType::setOne );
		// process
		region.randomAccess();
		// test
		Views.iterable( region ).forEach( x -> assertTrue(x.get()) );
	}

}
