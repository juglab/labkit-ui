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

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import sc.fiji.labkit.ui.LabkitFrame;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.inputimage.SpimDataInputException;
import sc.fiji.labkit.ui.inputimage.SpimDataInputImage;
import bdv.export.ProgressWriter;
import sc.fiji.labkit.ui.utils.progress.StatusServiceProgressWriter;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Labkit > Open Image File With Labkit")
public class LabkitImportPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private File file;

	@Override
	public void run() {
		run(context, file);
	}

	private static void run(Context context, File file) {
		try {
			ProgressWriter progressWriter = new StatusServiceProgressWriter(context
				.service(StatusService.class));
			InputImage image = openImage(context, progressWriter, file);
			LabkitFrame.showForImage(context, image);
		}
		catch (SpimDataInputException e) {
			JOptionPane.showMessageDialog(null, "There was an error when opening: " + file +
				"\n\n" + e.getMessage(), "Problem", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static InputImage openImage(Context context, ProgressWriter progressWriter,
		File file)
	{
		String filename = file.getAbsolutePath();
		if (filename.endsWith(".h5"))
			filename = filename.replaceAll("\\.h5$", ".xml");
		if (filename.endsWith(".czi"))
			return new CziOpener(progressWriter).openWithDialog(file.getAbsolutePath());
		if (filename.endsWith(".xml") || filename.endsWith(".ims"))
			return SpimDataInputImage.openWithGuiForLevelSelection(filename);
		try {
			Dataset dataset = context.service(DatasetIOService.class).open(new FileLocation(file));
			DatasetInputImage datasetInputImage = new DatasetInputImage(dataset);
			datasetInputImage.setDefaultLabelingFilename(filename + ".labeling");
			return datasetInputImage;
		}
		catch (IOException e) {
			throw new UnsupportedOperationException(
				"Could not open the image file: " + file);
		}
	}

	public static void main(String... args) {
		// demo
		final CommandService commandService = new Context().service(
			CommandService.class);
		commandService.run(LabkitImportPlugin.class, true);
	}
}
