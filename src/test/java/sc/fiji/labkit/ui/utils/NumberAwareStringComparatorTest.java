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

package sc.fiji.labkit.ui.utils;

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
