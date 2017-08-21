package net.imglib2.atlas.labeling;

import com.google.gson.Gson;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by arzt on 21.08.17.
 */
public class LabelingSerializationTest {

	@Test
	public void test() {
		Labeling labeling = exampleLabeling();
		String json = new Gson().toJson(labeling, Labeling.class);
		Labeling deserialized = new Gson().fromJson(json, Labeling.class);
		assertTrue(labelingsEqual(labeling, deserialized));
	}

	private boolean labelingsEqual(Labeling expected, Labeling actual) {
		// TODO equality test for region
		return Intervals.equals(expected, actual) && expected.regions().keySet().equals(actual.regions().keySet());
	}

	private static Labeling exampleLabeling() {
		IterableRegion<BitType> region1 = exampleRegion(10, 10);
		IterableRegion<BitType> region2 = exampleRegion(42, 12);
		Map<String, IterableRegion<BitType>> regions = new TreeMap<>();
		regions.put("A", region1);
		regions.put("B", region2);
		return new Labeling(regions, region1);
	}

	private static IterableRegion<BitType> exampleRegion(long... position) {
		SparseRoi roi = new SparseRoi(new FinalInterval(100, 200));
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(position);
		ra.get().set(true);
		return roi;
	}

}
