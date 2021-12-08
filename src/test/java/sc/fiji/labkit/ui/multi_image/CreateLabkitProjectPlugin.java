
package sc.fiji.labkit.ui.multi_image;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class,
	menuPath = "Plugins > Labkit > Project > Create New Labkit Project...")
public class CreateLabkitProjectPlugin implements Command {

	@Parameter
	private Context context;

	@Override
	public void run() {
		LabkitProjectFrame.onNewProjectClicked(context);
	}
}
