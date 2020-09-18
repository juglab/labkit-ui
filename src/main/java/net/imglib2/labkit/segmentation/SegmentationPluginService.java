
package net.imglib2.labkit.segmentation;

import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that returns a list of all segmentation plugins.
 */
@Plugin(type = SciJavaService.class)
public class SegmentationPluginService extends AbstractService implements SciJavaService {

	@Parameter
	private PluginService pluginService;

	public List<SegmentationPlugin> getSegmentationPlugins() {
		List<SegmentationPlugin> result = new ArrayList<>();
		for (PluginInfo<SegmentationPlugin> pi : pluginService.getPluginsOfType(
			SegmentationPlugin.class))
		{
			try {
				SegmentationPlugin instance = pi.createInstance();
				context().inject(instance);
				result.add(instance);
			}
			catch (InstantiableException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
