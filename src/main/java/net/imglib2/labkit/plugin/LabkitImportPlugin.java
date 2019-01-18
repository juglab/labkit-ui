
package net.imglib2.labkit.plugin;

import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import bdv.export.ProgressWriter;
import net.imglib2.labkit.utils.progress.StatusServiceProgressWriter;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Segmentation > Labkit (CZI / HDF5 / experimental)")
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
		ProgressWriter progressWriter = new StatusServiceProgressWriter(context
			.service(StatusService.class));
		InputImage image = openImage(progressWriter, file);
		MainFrame.showForImage(context, image);
	}

	private static InputImage openImage(ProgressWriter progressWriter,
		File file)
	{
		String filename = file.getAbsolutePath();
		if (filename.endsWith(".czi")) return new CziOpener(progressWriter)
			.openWithDialog(file.getAbsolutePath());
		if (filename.endsWith(".xml")) return new SpimDataInputImage(filename);
		throw new UnsupportedOperationException(
			"Only files with extension czi / xml are supported.");
	}

	public static void main(String... args) {
		// demo
		final CommandService commandService = new Context().service(
			CommandService.class);
		commandService.run(LabkitImportPlugin.class, true);
	}
}
