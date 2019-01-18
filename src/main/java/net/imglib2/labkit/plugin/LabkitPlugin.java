
package net.imglib2.labkit.plugin;

import net.imagej.Dataset;
import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Labkit")
public class LabkitPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private Dataset dataset;

	@Override
	public void run() {
		DatasetInputImage input = new DatasetInputImage(dataset);
		MainFrame.showForImage(context, input);
	}
}
