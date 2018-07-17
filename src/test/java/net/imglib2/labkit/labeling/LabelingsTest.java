package net.imglib2.labkit.labeling;

import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class LabelingsTest {

	@Test
	public void testOf() {
		ImgLabeling<String, ?> input = LabelingSerializer.fromImageAndLabelSets(
				ArrayImgs.unsignedBytes(new byte[]{2}, 1),
				Arrays.asList(Collections.emptySet(), Collections.singleton("a"), Collections.singleton("b"))
		);
		Labeling labeling = Labelings.of(input);
		assertEquals(Arrays.asList("b"), labeling.getLabels());
	}
}
