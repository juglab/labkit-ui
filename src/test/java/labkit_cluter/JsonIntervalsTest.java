
package labkit_cluter;

import net.imglib2.FinalInterval;
import labkit_cluster.JsonIntervals;
import net.imglib2.util.Intervals;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonIntervalsTest {

	FinalInterval interval = FinalInterval.createMinSize(10, 10, 5, 5);
	String json = "{\"min\":[10,10],\"max\":[14,14],\"n\":2}";

	@Test
	public void testToJson() {
		assertEquals(json, JsonIntervals.toJson(interval));
	}

	@Test
	public void testFromJson() {
		assertTrue(Intervals.equals(interval, JsonIntervals.fromJson(json)));
	}
}
