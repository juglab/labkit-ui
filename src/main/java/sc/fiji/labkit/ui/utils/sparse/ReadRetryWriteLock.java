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

import java.util.concurrent.locks.ReadWriteLock;

/**
 * The {@link ReadRetryWriteLock} is used in {@link SparseRandomAccessIntType}
 * to make the read and write access to
 * {@link gnu.trove.map.hash.TLongIntHashMap} thread safe, while maintaining
 * good performance.
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
