
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
