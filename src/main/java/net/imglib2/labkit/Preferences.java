
package net.imglib2.labkit;

import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class Preferences {

	private final PrefService prefService;

	public Preferences(Context context) {
		this.prefService = context.service(PrefService.class);
	}

	private static final String KEY = "labkit.default_labels";

	public List<String> getDefaultLabels() {
		String s = prefService.get(KEY);
		return (s == null || s.isEmpty()) ? Arrays.asList("background",
			"foreground") : Arrays.asList(s.split(","));
	}

	public void setDefaultLabels(List<String> labels) {
		StringJoiner joiner = new StringJoiner(",");
		labels.forEach(joiner::add);
		prefService.put(KEY, joiner.toString());
	}
}
