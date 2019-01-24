
package labkit_cluter;

import labkit_cluster.JsonIntervals;
import labkit_cluster.LabkitClusterCommand;
import labkit_cluster.MyN5;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.inputimage.SpimDataInputImage;
import net.imglib2.util.Intervals;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LabkitClusterCommandTest {

	private Context context = new Context();
	private CommandService commandService = context.service(CommandService.class);

	@Test
	public void test() throws ExecutionException, InterruptedException {
		final String outputPath = "/home/arzt/tmp/output/result.xml";
		final String inputXml =
			"/home/arzt/Documents/Datasets/Mouse Brain/hdf5/export.xml";
		final String value =
			"/home/arzt/Documents/Datasets/Mouse Brain/hdf5/classifier.classifier";
		final long[] dimensions = Intervals.dimensionsAsLongArray(
			new SpimDataInputImage(inputXml, 0).interval());
		CellGrid grid = new CellGrid(dimensions, new int[] { 100, 100, 100 });
		MyN5.createDataset(outputPath, grid);
		Map<String, Object> map = initializeParameters(outputPath, inputXml, value,
			grid);
		Future<CommandModule> result = commandService.run(
			LabkitClusterCommand.class, false, map);
		result.get();
		System.out.println("Results written to: " + outputPath);
	}

	private Map<String, Object> initializeParameters(String outputPath,
		String inputXml, String value, CellGrid grid)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("input", inputXml);
		map.put("output", outputPath);
		map.put("classifier", value);
		List<Interval> cells = gridAsList(grid);
		map.put("interval", JsonIntervals.toJson(cells.get(0)));
		return map;
	}

	private List<Interval> gridAsList(CellGrid grid) {
		int numCells = (int) Intervals.numElements(grid.getGridDimensions());
		int n = grid.numDimensions();
		return new AbstractList<Interval>() {

			@Override
			public Interval get(int index) {
				long[] min = new long[n];
				long[] max = new long[n];
				int[] size = new int[n];
				grid.getCellDimensions(index, min, size);
				for (int i = 0; i < min.length; i++)
					max[i] = min[i] + size[i];
				return new FinalInterval(min, max);
			}

			@Override
			public int size() {
				return numCells;
			}
		};
	}
}
