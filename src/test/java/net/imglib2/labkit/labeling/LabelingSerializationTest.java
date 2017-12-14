package net.imglib2.labkit.labeling;

import com.google.gson.Gson;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class LabelingSerializationTest {

	@Test
	public void test() {
		Labeling labeling = exampleLabeling();
		String json = new Gson().toJson(labeling, Labeling.class);
		Labeling deserialized = new Gson().fromJson(json, Labeling.class);
		assertTrue(labelingsEqual(labeling, deserialized));
	}

	@Test
	public void testOpenAndSaveToTiff() throws IOException {
		Labeling labeling = exampleLabeling();
		LabelingSerializer serializer = new LabelingSerializer(new Context());
		serializer.save(labeling, "test.tif");
		Labeling deserialized = serializer.open("test.tif");
		assertTrue(labelingsEqual(labeling, deserialized));
	}

	private boolean labelingsEqual(Labeling expected, Labeling actual) {
		boolean[] value = {true};
		Views.interval(Views.pair(expected, actual), expected).forEach(
				p -> { value[0] &= setsEqual(p.getA(), p.getB()); }
		);
		return value[0];
	}

	private boolean setsEqual(Set<String> a, Set<String> b) {
		return a.size() == b.size() && a.containsAll(b);
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
		SparseIterableRegion roi = new SparseIterableRegion(new FinalInterval(100, 200));
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(position);
		ra.get().set(true);
		return roi;
	}
}
