/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.labeling;

import com.google.gson.internal.LinkedTreeMap;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgSaver;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.IterableRegion;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;
import net.imglib2.test.ImgLib2Assert;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class LabelingSerializationTest {

	private final LabelingSerializer serializer = new LabelingSerializer(SingletonContext
		.getInstance());

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

	@Test
	public void testOpenFromTiff() throws IOException {
		Img<UnsignedByteType> input = ArrayImgs.unsignedBytes(new byte[] { 10, 20, 0, 0 }, 2, 2);
		Path file = Files.createTempFile("test", ".tif");
		new ImgSaver(SingletonContext.getInstance()).saveImg(file.toString(), input, new SCIFIOConfig()
			.writerSetFailIfOverwriting(false));
		Labeling labeling = serializer.open(file.toString());
		assertEquals(Arrays.asList("10", "20"), labeling.getLabels().stream().map(Label::name).collect(
			Collectors.toList()));
		RandomAccessibleInterval<BitType> a = labeling.getRegion(labeling.getLabel("10"));
		ImgLib2Assert.assertImageEqualsRealType(ArrayImgs.unsignedBytes(new byte[] { 1, 0, 0, 0 }, 2,
			2), a, 0.0);
	}
}
