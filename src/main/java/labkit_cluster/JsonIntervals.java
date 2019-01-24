
package labkit_cluster;

import com.google.gson.Gson;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

public class JsonIntervals {

	public static String toJson(Interval interval) {
		return new Gson().toJson(new FinalInterval(interval));
	}

	public static Interval fromJson(String json) {
		return new Gson().fromJson(json, FinalInterval.class);
	}
}
