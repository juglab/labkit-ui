
package sc.fiji.labkit.ui.utils.sparse;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ReadRetryWriteLockTest {

	private final Random random = new Random();

	private volatile int data = 0;

	private volatile boolean stop = false;

	private volatile long readMistakes = 0;

	private final ReadRetryWriteLock lock = new ReadRetryWriteLock();

	@Test
	public void test() throws InterruptedException {
		new Thread(this::writerThread).start();
		new Thread(this::readerThread).start();
		Thread.sleep(100);
		stop = true;
		assertEquals(0, readMistakes);
	}

	private void writerThread() {
		while (!stop)
			threadSafeWrite();
	}

	private void readerThread() {
		while (!stop)
			if (threadSafeRead() % 2 != 0)
				readMistakes++;
	}

	private void threadSafeWrite() {
		synchronized (lock) {
			lock.writeLock();
			try {
				writeData();
			}
			finally {
				lock.writeUnlock();
			}
		}
	}

	private int threadSafeRead() {
		while (true) {
			long readId = lock.startRead();
			int result = readData();
			if (lock.isReadValid(readId))
				return result;
		}
	}

	private void writeData() {
		data = random.nextInt(500);
		data *= 2;
	}

	private int readData() {
		return data;
	}

	@Test(expected = AssertionError.class)
	public void testWriteLockRequiresSynchronized() {
		lock.writeLock();
	}

	@Test(expected = AssertionError.class)
	public void testWriteUnlockRequiresSynchronized() {
		lock.writeUnlock();
	}
}
