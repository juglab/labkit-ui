package net.imglib2.labkit.plugin;

import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Labkit (CZI / HDF5 / experimental)")
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
		InputImage image = openImage( file );
		new MainFrame(context, image);
	}

	private static InputImage openImage( File file )
	{
		String filename = file.getAbsolutePath();
		if(filename.endsWith( ".czi" ))
			return new DatasetInputImage( BFTiledImport.openImage( filename ));
		if(filename.endsWith( ".xml" ))
			return new SpimDataInputImage( filename );
		throw new UnsupportedOperationException( "Only files with extension czi / hdf5 are supported." );
	}

	public static void main(String... args) {
		final String mouse = "/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		final String xwing = "/home/arzt/Documents/Datasets/XWing/xwing.xml";
		final String lung = "/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0007.czi";
		run(new Context(), new File( mouse ));
	}
}
