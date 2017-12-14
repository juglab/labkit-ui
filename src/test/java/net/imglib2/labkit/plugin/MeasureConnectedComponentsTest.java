package net.imglib2.labkit.plugin;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MeasureConnectedComponentsTest {

	int[] data = {
			0,0,0,0,0,0,0,0,0,0,
			0,1,1,1,0,0,0,0,0,0,
			0,1,0,1,0,0,0,0,0,0,
			0,1,1,1,0,0,1,0,0,0,
			0,0,0,0,1,0,0,0,0,0,
			0,1,1,1,0,0,0,0,0,0,
			0,1,1,1,0,1,1,0,0,0,
			0,1,1,1,0,0,0,0,0,0,
			0,1,1,1,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0
	};

	IterableRegion<BitType> input = init();

	private IterableRegion<BitType> init() {
		Img<IntType> ints = ArrayImgs.ints(data, 10, 10);
		SparseIterableRegion sparse = new SparseIterableRegion(Intervals.createMinSize(0, 0, 10, 10));
		Cursor<IntType> cursor = ints.cursor();
		while(cursor.hasNext())
			if(cursor.next().get() != 0)
				sparse.add(cursor);
		return sparse;
	}

	@Test
	public void test() {
		List<Long> result = MeasureConnectedComponents.connectedComponets(input);
		result.sort(Long::compare);
		assertEquals(Arrays.asList(1L, 1L, 2L, 8L, 12L), result);
	}

	public static void main(String... args) {
		new MeasureConnectedComponentsTest().test();
	}
}
