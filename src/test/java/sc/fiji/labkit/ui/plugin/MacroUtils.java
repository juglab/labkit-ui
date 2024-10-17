/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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
package sc.fiji.labkit.ui.plugin;

import java.io.File;

public final class MacroUtils {

	private MacroUtils() { }

	/**
	 * Escapes backslashes in file paths (which is crucial on Windows)
	 * so they work as desired when embedded into an ImageJ macro.
	 */
	public static String safePath(File file) {
		// This regex replacement is tricky:
		//
		// 1. We must escape the Java strings, because the expression
		//    "\\" represents the literal backslash character '\'.
		//
		// 2. Then we must use a second level of escaping for the regex,
		//    because we need to pass `\\` as the pattern to match or else
		//    the regex logic won't understand we're talking about a
		//    literal backslash rather than an escaped something-or-other.
		//
		// Together, this expression replaces single literal backslashes
		// with double literal backslashes.
		//
		// We then we end up with an ImageJ macro that has a `\\` for
		// each backslash within the embedded paths, and ImageJ's
		// macro execution logic unescapes them accordingly.

		return file.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
	}
}
