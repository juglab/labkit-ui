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

package sc.fiji.labkit.ui.plugin;

import bdv.export.ProgressWriterConsole;
import bdv.util.BdvOptions;
import com.google.common.io.PatternFilenameFilter;
import io.scif.img.ImgIOException;
import loci.formats.FormatException;
import net.imglib2.exception.IncompatibleTypeException;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;

import java.awt.*;
import java.io.IOException;

public class CziOpenerDemo {

	public static void main(String... args) throws IOException, FormatException,
		IncompatibleTypeException, ImgIOException
	{
		String filename = filenameFromCommandLineOrDialog(args);
		run(filename);
	}

	public static void run(String filename) {
		CziOpener opener = new CziOpener(new ProgressWriterConsole());
		DatasetInputImage out = opener.openWithDialog(filename);
		out.showable().show("Image", BdvOptions.options().is2D());
	}

	private static String filenameFromCommandLineOrDialog(String[] args) {
		if (args.length == 1) return args[0];
		else {
			FileDialog dialog = new FileDialog((Frame) null,
				"CZI Demo, please open a CZI image");
			dialog.setFilenameFilter(new PatternFilenameFilter(".*\\.czi"));
			dialog.setVisible(true);
			return dialog.getFiles()[0].getAbsolutePath();
		}
	}
}
