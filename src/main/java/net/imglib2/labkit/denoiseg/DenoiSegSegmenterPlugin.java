
package net.imglib2.labkit.denoiseg;

import de.csbdresden.denoiseg.command.DenoiSegPredictCommand;
import de.csbdresden.denoiseg.command.DenoiSegTrainCommand;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.SegmentationPluginService;
import net.imglib2.labkit.segmentation.Segmenter;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.HashMap;

@Plugin(type = SegmentationPlugin.class)
public class DenoiSegSegmenterPlugin implements SegmentationPlugin {

	@Parameter
	public Context context;

	@Parameter
	public LogService logService;

	@Override
	public String getTitle() {
		return "DenoiSeg";
	}

	@Override
	public Segmenter createSegmenter() {

		//logService.log(0,"Create segmenter");

		//commandService.run(DenoiSegTrainCommand.class, true, new HashMap<String, Object>());

		return new DenoiSegSegmenter(context);
	}
}
