
package net.imglib2.labkit.plugin;

import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import net.imglib2.labkit.utils.ProgressConsumer;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
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
		ProgressConsumer progressConsumer = context.service(
			StatusService.class)::showProgress;
		InputImage image = openImage(progressConsumer, file);
		new MainFrame(context, image);
	}

	private static InputImage openImage(ProgressConsumer progressConsumer,
		File file)
	{
		String filename = file.getAbsolutePath();
		if (filename.endsWith(".czi")) return new CziOpener(progressConsumer)
			.openWithDialog(file.getAbsolutePath());
		if (filename.endsWith(".xml")) return new SpimDataInputImage(filename);
		throw new UnsupportedOperationException(
			"Only files with extension czi / xml are supported.");
	}

	public static void main(String... args) {
		final String mouse =
			"/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		final String xwing = "/home/arzt/Documents/Datasets/XWing/xwing.xml";
		final String lung =
			"/home/arzt/Documents/Datasets/Lung Images/labeled/2017_11_30__0033.czi";
		run(new Context(), new File(lung));
	}
}
