
package net.imglib2.labkit.labeling;

import com.google.gson.internal.LinkedTreeMap;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import org.junit.Test;
import org.scijava.Context;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthias Arzt
 */
public class LabelingSerializationTest {

	@Test
	public void testJson() throws IOException {
		testSerialization(exampleLabeling(), "json");
		testSerialization(emptyLabeling(), "json");
	}

	@Test
	public void testColorsAndLabelOrder() throws IOException {
		Labeling expected = exampleLabeling();
		expected.getLabel("B").setColor(new ARGBType(Color.yellow.getRGB()));
		expected.getLabel("A").setColor(new ARGBType(Color.green.getRGB()));
		final String filename = tempFileWithExtension("json");
		LabelingSerializer serializer = new LabelingSerializer(new Context());
		serializer.save(expected, filename);
		Labeling actual = serializer.open(filename);
		assertColorsAndLabelOrderMatches(expected.getLabels(), actual.getLabels());
	}

	@Test
	public void testTif() throws IOException {
		testSerialization(exampleLabeling(), "tif");
		testSerialization(emptyLabeling(), "tif");
	}

	private void testSerialization(Labeling labeling, String extension)
		throws IOException
	{
		final String filename = tempFileWithExtension(extension);
		LabelingSerializer serializer = new LabelingSerializer(new Context());
		serializer.save(labeling, filename);
		Labeling deserialized = serializer.open(filename);
		ImgLib2Assert.assertImageEquals(labeling, deserialized, (a, b) -> setsEqual(toStrings(a),
			toStrings(b)));
	}

	private String tempFileWithExtension(String extension) throws IOException {
		File file = File.createTempFile("test-", "." + extension);
		file.deleteOnExit();
		return file.getAbsolutePath();
	}

	private void assertColorsAndLabelOrderMatches(List<Label> expected, List<Label> actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertLabelEquals(expected.get(i), actual.get(i));
		}
	}

	private void assertLabelEquals(Label expected, Label actual) {
		assertEquals(expected.name(), actual.name());
		assertEquals(expected.color(), actual.color());
	}

	private Set<String> toStrings(Set<Label> input) {
		return input.stream().map(Label::name).collect(Collectors.toSet());
	}

	private <T> boolean setsEqual(Set<T> a, Set<T> b) {
		return a.size() == b.size() && a.containsAll(b);
	}

	private static Labeling exampleLabeling() {
		IterableRegion<BitType> region1 = exampleRegion(10, 10);
		IterableRegion<BitType> region2 = exampleRegion(42, 12);
		Map<String, IterableRegion<BitType>> regions = new LinkedTreeMap<>();
		regions.put("A", region1);
		regions.put("B", region2);
		Labeling labeling = Labeling.fromMap(regions);
		return labeling;
	}

	private static Labeling emptyLabeling() {
		return Labeling.createEmpty(Collections.emptyList(), new FinalInterval(2,
			2));
	}

	private static IterableRegion<BitType> exampleRegion(long... position) {
		SparseIterableRegion roi = new SparseIterableRegion(new FinalInterval(100,
			200));
		RandomAccess<BitType> ra = roi.randomAccess();
		ra.setPosition(position);
		ra.get().set(true);
		return roi;
	}
}
