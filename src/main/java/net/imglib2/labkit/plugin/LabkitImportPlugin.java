package net.imglib2.labkit.plugin;

import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
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
		BFTiledImport.Section section = BFTiledImport.openImage(file.getAbsolutePath());
		DatasetInputImage image = new DatasetInputImage(section.image);
		image.setLabelingName(getLabelingName(section));
		new MainFrame(context, image);
	}

	private static String getLabelingName(BFTiledImport.Section section) {
		String source = section.image.getSource();
		if(source.endsWith(".czi") && section.index != null) {
			return source.substring(0, source.length() - ".czi".length())
					+ "-" + (section.index + 1) + ".czi.labeling";
		}
		return source + ".labeling";
	}

	public static void main(String... args) {
		run(new Context(), new File("/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0007.czi"));
	}
}
