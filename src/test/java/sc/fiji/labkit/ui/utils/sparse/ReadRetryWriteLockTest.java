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
