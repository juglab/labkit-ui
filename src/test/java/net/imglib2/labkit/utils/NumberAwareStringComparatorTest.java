
package net.imglib2.labkit.utils;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link NumberAwareStringComparator}.
 */
public class NumberAwareStringComparatorTest {

	@Test
	public void test() {
		Comparator<CharSequence> comparator = NumberAwareStringComparator.getInstance();
		assertTrue(comparator.compare("file1", "file2") < 0);
		assertTrue(comparator.compare("file2", "file1") > 0);
		assertTrue(comparator.compare("file9", "file10") < 0);
		assertTrue(comparator.compare("file02", "file2") < 0);
		assertTrue(comparator.compare("file20", "file2") > 0);
		assertTrue(comparator.compare("9foobar", "10foobar") < 0);
		assertTrue(comparator.compare("9", "10") < 0);
		assertTrue(comparator.compare("hello1world2", "hello1world2") == 0);
		assertTrue(comparator.compare("file0", "file") > 0);
		assertTrue(comparator.compare("file", "file0") < 0);
		assertTrue("file".compareTo("file0") < 0);
	}
}
