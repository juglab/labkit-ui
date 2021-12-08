
package sc.fiji.labkit.ui;

import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Wrapper around {@link PrefService} to store preferences.
 */
public class Preferences {

	private final PrefService prefService;

	public Preferences(Context context) {
		this.prefService = context.service(PrefService.class);
	}

	private static final String KEY = "default_labels";

	public List<String> getDefaultLabels() {
		String s = prefService.get(Preferences.class, KEY);
		return (s == null || s.isEmpty()) ? Arrays.asList("background",
			"foreground") : Arrays.asList(s.split(","));
	}

	public void setDefaultLabels(List<String> labels) {
		StringJoiner joiner = new StringJoiner(",");
		labels.forEach(joiner::add);
		prefService.put(Preferences.class, KEY, joiner.toString());
	}
}
