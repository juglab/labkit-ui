package net.imglib2.atlas.plugin;

import net.imagej.Dataset;
import net.imglib2.atlas.MainFrame;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Command.class, menuPath = "Plugins > Segmentation > Labkit (Time Series)")
public class LabkitTimeSeriesPlugin implements Command {

	@Parameter
	private Context context;

	@Parameter
	private Dataset dataset;

	@Override
	public void run() {
		new MainFrame(context, dataset, true);
	}
}
