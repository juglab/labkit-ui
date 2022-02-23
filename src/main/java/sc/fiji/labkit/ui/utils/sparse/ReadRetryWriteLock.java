
package sc.fiji.labkit.ui.utils.sparse;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * The {@link ReadRetryWriteLock} is used in {@link SparseRandomAccessIntType}
 * to make the read & write access to {@link gnu.trove.map.hash.TLongIntHashMap}
 * thread safe, while maintaining good performance.
 * <p>
 * The {@link ReadRetryWriteLock} can be used to make a data structure thread
 * safe, that fails to read correctly during an ongoing write operation. It is
 * in that sense similar to a {@link ReadWriteLock}, but achieves better
 * performance. It has an additional requirement though: The read operation,
 * when performed during a write operation, may return a wrong value, but it is
 * not allowed to change the state of the data structure or throw an exception.
 * <p>
 * The basic idea behind the {@link ReadRetryWriteLock} is to repeat a read
 * operation, when it's performed during a write operation.
 * <p>
 * Read and write operations that fulfill the requirements above can be made
 * thread safe by wrapping them as shown in the code below.
 * 
 * <pre>
 *     {@code
 *     ReadRetryWriteLock lock = new ReadRetryWriteLock();
 *
 *     private void safeWrite() {
 *        synchronized(lock) {
 *            lock.writeLock();
 *            try {
 *                writeOperation();
 *            }
 *            finally {
 *                lock.writeUnlock();
 *            }
 *        }
 *     }
 *
 *     private Object safeRead() {
 *         while(true) {
 *             long readId = lock.startRead();
 *             Object result = readOperation();
 *             if(lock.read(readId))
 *                 return result;
 *         }
 *     }
 *     }
 * </pre>
 */
public class ReadRetryWriteLock {

	private volatile long modifications = 0;

	public long startRead() {
		while (true) {
			long readId = modifications;
			boolean success = (readId % 2 == 0);
			if (success)
				return readId;
			waitForWriteUnlock();
		}
	}

	public boolean isReadValid(long readId) {
		return readId == modifications;
	}

	public void writeLock() {
		assert Thread.holdsLock(this);
		long l = ++modifications;
		assert l % 2 == 1;
	}

	public void writeUnlock() {
		assert Thread.holdsLock(this);
		long l = ++modifications;
		assert l % 2 == 0;
	}

	private synchronized void waitForWriteUnlock() {

	}
}
