package net.imglib2.labkit.plugin;

import net.imagej.Dataset;
import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Labkit (CZI / experimental)")
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
		DefaultInputImage image = new DefaultInputImage(BFTiledImport.openImage(file.getAbsolutePath()));
		image.setFilename(file.getAbsolutePath());
		new MainFrame(context, image);
	}

	public static void main(String... args) {
		run(new Context(), new File("/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0007.czi"));
	}
}
