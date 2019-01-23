
package labkit_cluter;

import labkit_cluster.JsonIntervals;
import labkit_cluster.LabkitClusterCommand;
import net.imglib2.FinalInterval;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandService;

import java.util.HashMap;
import java.util.Map;

public class LabkitClusterCommandTest {

	private Context context = new Context();
	private CommandService commandService = context.service(CommandService.class);

	@Test
	public void test() {
		Map<String, Object> map = new HashMap<>();
		map.put("input",
			"/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.h5");
		map.put("output", "/home/arzt/tmp/output/result.xml");
		map.put("classifier",
			"/home/arzt/Documents/Datasets/Mouse Brain/hdf5/classifier.classifier");
		map.put("interval", JsonIntervals.toJson(new FinalInterval(100, 100, 100)));
		commandService.run(LabkitClusterCommand.class, false, map);
	}
}
