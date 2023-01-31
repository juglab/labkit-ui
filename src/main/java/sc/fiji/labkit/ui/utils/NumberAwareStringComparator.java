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

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Comparator} for comparing {@link String}s that works better for
 * numbers than {@link String#compareTo(String)} does.
 * <p>
 * It will correctly sort "file9.tif" &lt; "file10.tif".
 */
public class NumberAwareStringComparator implements Comparator<CharSequence> {

	public static Comparator<CharSequence> getInstance() {
		return INSTANCE;
	}

	private static final NumberAwareStringComparator INSTANCE =
		new NumberAwareStringComparator();

	private static final Pattern PATTERN = Pattern.compile("(\\D*)(\\d*)");

	private NumberAwareStringComparator() {}

	public int compare(CharSequence a, CharSequence b) {
		Matcher matcherA = PATTERN.matcher(a);
		Matcher matcherB = PATTERN.matcher(b);

		// The only way find() could fail is at the end of a string
		while (matcherA.find() && matcherB.find()) {
			// matcher.group(1) fetches any non-digits captured by the
			// first parentheses in PATTERN.
			int nonDigitCompare = matcherA.group(1).compareTo(matcherB.group(1));
			if (0 != nonDigitCompare) {
				return nonDigitCompare;
			}

			// matcher.group(2) fetches any digits captured by the
			// second parentheses in PATTERN.
			String digitsA = matcherA.group(2);
			String digitsB = matcherB.group(2);
			if (digitsA.isEmpty()) {
				return digitsB.isEmpty() ? 0 : -1;
			}
			else if (digitsB.isEmpty()) {
				return +1;
			}

			int numberCompare = compareNumbers(digitsA, digitsB);
			if (0 != numberCompare) {
				return numberCompare;
			}
		}

		// Handle if one string is a prefix of the other.
		// Nothing comes before something.
		return matcherA.hitEnd() && matcherB.hitEnd() ? 0 : matcherA.hitEnd() ? -1 : +1;
	}

	private int compareNumbers(String digitsA, String digitsB) {
		BigInteger n1 = new BigInteger(digitsA);
		BigInteger n2 = new BigInteger(digitsB);
		final int result = n1.compareTo(n2);
		if (result != 0)
			return result;
		return digitsA.compareTo(digitsB);
	}
}
