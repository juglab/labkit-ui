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

package sc.fiji.labkit.ui.progress;

import sc.fiji.labkit.ui.utils.progress.PrintStreamToLines;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PrintStreamToLinesTest {

	@Test
	public void testSplit() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("Hello\nWorld\n");
		assertEquals(Arrays.asList("Hello", "World"), lines);
	}

	@Test
	public void testPrefix() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("Hello");
		out.print(" ");
		out.print("World!\n");
		assertEquals(Arrays.asList("Hello World!"), lines);
	}

	@Test
	public void testEmptyLines() {
		List<String> lines = new ArrayList<>();
		PrintStream out = PrintStreamToLines.printStreamToLines(lines::add);
		out.print("\n\na\n\n");
		assertEquals(Arrays.asList("", "", "a", ""), lines);
	}
}
