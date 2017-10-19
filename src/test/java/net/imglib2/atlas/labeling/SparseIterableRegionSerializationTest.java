package net.imglib2.atlas.labeling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.imglib2.*;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Mattias Arzt
 */
public class SparseIterableRegionSerializationTest {
	@Test
	public void test() {
		final Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(IterableRegion.class, new SparseIterableRegionSerializer.Adapter())
				.create();
		final SparseIterableRegion roi = exampleSparseRoi();
		String json = gson.toJson(roi);
		SparseIterableRegion roi2 = gson.fromJson(json, SparseIterableRegion.class);
		assertImagesEqual(roi, roi2);
		assertEquals(roi.size(), roi2.size());
	}

	public static SparseIterableRegion exampleSparseRoi() {
		final Interval interval = new FinalInterval(100,200,300);
		final SparseIterableRegion roi = new SparseIterableRegion(interval);
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(new long[]{42, 42, 42});
		ra.get().set(true);
		ra.setPosition(new long[]{1, 2, 3});
		ra.get().set(true);
		return roi;
	}

	public static <A extends Type<A>>
	void assertImagesEqual(final RandomAccessibleInterval<? extends A> a, final RandomAccessibleInterval<? extends A> b) {
		assertTrue(Intervals.equals(a, b));
		System.out.println("check picture content.");
		IntervalView<? extends Pair<? extends A, ? extends A>> pairs = Views.interval(Views.pair(a, b), b);
		Cursor<? extends Pair<? extends A, ? extends A>> cursor = pairs.cursor();
		while(cursor.hasNext()) {
			Pair<? extends A,? extends A> p = cursor.next();
			boolean equal = p.getA().valueEquals(p.getB());
			if(!equal)
				fail("Pixel values not equal on coordinate " + ", expected: "
						+ p.getA() + " actual: " + p.getB());
		}
	}
}
