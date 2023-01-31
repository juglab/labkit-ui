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

package sc.fiji.labkit.ui.utils.sparse;

import gnu.trove.map.hash.TLongIntHashMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests if {@link ReadRetryWriteLock} can be used to make a
 * {@link TLongIntHashMap} thread safe.
 */
public class ThreadSafeLongIntHasMapTest {

	private final ThreadSafeLongIntHashMap map = new ThreadSafeLongIntHashMap();

	private final Thread writerThread = new Thread(this::writeManyValues);

	@Test
	public void test() throws InterruptedException {
		map.put(1, 1);
		writerThread.start();
		long mistakes = readAndCountMistakes();
		assertEquals(0, mistakes);
	}

	private void writeManyValues() {
		for (int i = 2; i < 1000_000; i++) {
			map.put(i, i);
		}
	}

	private long readAndCountMistakes() {
		long mistakes = 0;
		while (writerThread.isAlive())
			if (map.get(1) != 1)
				mistakes++;
		return mistakes;
	}

	private static class ThreadSafeLongIntHashMap {

		private final ReadRetryWriteLock lock = new ReadRetryWriteLock();

		private final TLongIntHashMap map = new TLongIntHashMap();

		public void put(long key, int value) {
			synchronized (lock) {
				lock.writeLock();
				try {
					map.put(key, value);
				}
				finally {
					lock.writeUnlock();
				}
			}
		}

		public int get(int key) {
			while (true) {
				try {
					long readId = lock.startRead();
					int value = map.get(key);
					if (lock.isReadValid(readId))
						return value;
				}
				catch (ArrayIndexOutOfBoundsException e) {
					// retry read;
				}
			}
		}

	}
}
