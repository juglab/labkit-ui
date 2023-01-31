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

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;

/**
 * @author Matthias Arzt
 */
public class MappingCursor<T> implements Cursor<T> {

	private final Cursor<?> cursor;

	private final RandomAccess<T> randomAccess;

	public MappingCursor(Cursor<?> cursor, RandomAccess<T> randomAccess) {
		this.cursor = cursor;
		this.randomAccess = randomAccess;
	}

	@Override
	public Cursor<T> copyCursor() {
		return new MappingCursor<>(cursor.copyCursor(), randomAccess
			.copyRandomAccess());
	}

	@Override
	public T next() {
		fwd();
		return get();
	}

	@Override
	public void jumpFwd(long steps) {
		cursor.jumpFwd(steps);
		randomAccess.setPosition(cursor);
	}

	@Override
	public void fwd() {
		cursor.fwd();
		randomAccess.setPosition(cursor);
	}

	@Override
	public void reset() {
		cursor.reset();
		randomAccess.setPosition(cursor);
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}

	@Override
	public void localize(int[] position) {
		cursor.localize(position);
	}

	@Override
	public void localize(long[] position) {
		cursor.localize(position);
	}

	@Override
	public int getIntPosition(int d) {
		return cursor.getIntPosition(d);
	}

	@Override
	public long getLongPosition(int d) {
		return cursor.getLongPosition(d);
	}

	@Override
	public void localize(float[] position) {
		cursor.localize(position);
	}

	@Override
	public void localize(double[] position) {
		cursor.localize(position);
	}

	@Override
	public float getFloatPosition(int d) {
		return cursor.getFloatPosition(d);
	}

	@Override
	public double getDoublePosition(int d) {
		return cursor.getDoublePosition(d);
	}

	@Override
	public int numDimensions() {
		return cursor.numDimensions();
	}

	@Override
	public T get() {
		return randomAccess.get();
	}

	@Override
	public Sampler<T> copy() {
		return randomAccess.copy();
	}
}
