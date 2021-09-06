
package net.imglib2.labkit.plugin;

import bdv.img.imaris.Imaris;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputException;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import bdv.export.ProgressWriter;
import net.imglib2.labkit.utils.progress.StatusServiceProgressWriter;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.FileUtils;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;

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
			return new DatasetInputImage(dataset);
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
