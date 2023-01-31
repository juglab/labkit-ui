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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

// @formatter:off
/**
 * A simple cache, that allows to cache the values returned by an
 * {@code IntFunction<T>}.
 * <p>
 * Here is an example. The following code will print "Hello number 42" twice.
 * But the function "helloFunction" is only run once:
 * <pre>
 * {@code
 * IntFunction<String> helloFunction = i -> "Hello number " + i;
 * IntFunction<String> cachedFunction = new ConcurrentIntFunctionCache(helloFunction);
 * System.out.println(cachedFunction.apply(42));
 * System.out.println(cachedFunction.apply(42));
 * }
 * </pre>
 * <p>
 * Thread safety: Lets consider the example above. If the function
 * "helloFunction" is thread safe, then the "cachedFunction" is also thread
 * safe.
 */
// @formatter:on
public class ConcurrentIntFunctionCache<T> implements IntFunction<T> {

	private final IntFunction<T> function;

	private Object[] values = new Object[16];

	public ConcurrentIntFunctionCache(IntFunction<T> function) {
		this.function = function;
	}

	@Override
	public T apply(int input) {
		if (input < 0)
			throw new IllegalArgumentException("input must be positive");
		if (input >= values.length)
			grow(input);

		@SuppressWarnings("unchecked")
		T output = (T) values[input];
		if (output == null) {
			output = Objects.requireNonNull(function.apply(input));
			values[input] = output;
		}
		return output;
	}

	private synchronized void grow(int input) {
		// check if the array was has been grown by another thread already.
		if (input < values.length)
			return;

		int capacity = values.length;
		while (capacity <= input)
			capacity *= 2;
		values = Arrays.copyOf(values, capacity);
	}
}
