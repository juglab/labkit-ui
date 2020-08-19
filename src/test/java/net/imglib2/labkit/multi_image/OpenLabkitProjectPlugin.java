
package net.imglib2.labkit.multi_image;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Labkit > Open Labkit Project...")
public class OpenLabkitProjectPlugin implements Command {

	@Parameter
	private Context context;

	@Override
	public void run() {
		LabkitProjectFrame.onOpenProjectClicked(context);
	}
}
