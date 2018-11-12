package net.imglib2.labkit_rest_api;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ParseUrlTest {
	@Test
	public void test() {
		long[] result = ImageResource.parseLongArray("1_3_5");
		assertArrayEquals(new long[]{1, 3, 5}, result);
	}
}
