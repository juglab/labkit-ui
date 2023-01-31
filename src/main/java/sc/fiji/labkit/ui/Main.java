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

package sc.fiji.labkit.ui;

import org.scijava.Context;
import org.scijava.ui.UIService;
import sc.fiji.labkit.ui.actions.AbstractFileIoAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author Matthias Arzt
 */
public class Main {

	public static void main(String... args) {
		String filename = readCommandline(args);
		if (filename == null) filename = showOpenDialog();
		if (filename != null) start(filename);
	}

	private static String readCommandline(String[] args) {
		switch (args.length) {
			case 1:
				return args[0];
			case 0:
				return null;
			default:
				System.err.println("USAGE:    ./start.sh {optional/path/to/image.tif}");
				System.exit(2);
		}
		throw new AssertionError();
	}

	private static String showOpenDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);

		FileFilter fileFilter = AbstractFileIoAction.TIFF_FILTER;
		fileChooser.setFileFilter(fileFilter);
		fileChooser.addChoosableFileFilter(fileFilter);

		final int returnVal = fileChooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) return null;
		return fileChooser.getSelectedFile().getAbsolutePath();
	}

	public static void start(String filename) {
		Context context = new Context();
		context.service(UIService.class).showUI();
		LabkitFrame.showForFile(context, filename);
	}
}
