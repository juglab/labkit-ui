
package labkit_cluster;

import net.imglib2.Interval;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class)
public class LabkitClusterCommand implements Command {

	@Parameter
	private String classifier;

	@Parameter
	private String input;

	@Parameter
	private String interval;

	@Parameter
	private String output;

	@Override
	public void run() {
		Interval interval = JsonIntervals.fromJson(this.interval);
		System.out.println(interval);
	}

}
