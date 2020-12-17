
package net.imglib2.labkit.plugin;

import net.imglib2.labkit.InitialLabeling;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import bdv.export.ProgressWriter;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.utils.progress.StatusServiceProgressWriter;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
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
	menuPath = "Plugins > Segmentation > Labkit > Open CZI / HDF5 (experimental)")
public class LabkitImportPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private File file;

	@Override
	public void run() {
		ProgressWriter progressWriter = new StatusServiceProgressWriter(context
			.service(StatusService.class));
		Pair<InputImage, String> imageAndLabelingFile = openImage(progressWriter, file);
		InputImage image = imageAndLabelingFile.getA();
		String labelingFile = imageAndLabelingFile.getB();
		final SegmentationModel model = new DefaultSegmentationModel(context, image);
		initializeLabeling(model, image, labelingFile);
		LabkitFrame.show(model, image.imageForSegmentation().getName());
	}

	private void initializeLabeling(SegmentationModel model, InputImage image,
		String labelingFilename)
	{
		model.imageLabelingModel().labeling().set(InitialLabeling.initialLabeling(context, image,
			labelingFilename));
	}

	private static Pair<InputImage, String> openImage(ProgressWriter progressWriter,
		File file)
	{
		String filename = file.getAbsolutePath();
		if (filename.endsWith(".czi")) return new CziOpener(progressWriter).openWithDialog(file
			.getAbsolutePath());
		if (filename.endsWith(".xml")) {
			SpimDataInputImage inputImage = SpimDataInputImage.openWithGuiForLevelSelection(filename);
			return new ValuePair<>(inputImage, file.toString() + ".labeling");
		}
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
