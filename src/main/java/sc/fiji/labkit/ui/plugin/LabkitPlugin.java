
package sc.fiji.labkit.ui.plugin;

import net.imagej.Dataset;
import sc.fiji.labkit.ui.LabkitFrame;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Labkit > Open Current Image With Labkit")
public class LabkitPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private Dataset dataset;

	@Override
	public void run() {
		DatasetInputImage input = new DatasetInputImage(dataset);
		LabkitFrame.showForImage(context, input);
	}
}
