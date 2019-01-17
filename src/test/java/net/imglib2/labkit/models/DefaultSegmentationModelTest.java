
package net.imglib2.labkit.models;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.type.logic.BitType;
import org.junit.Test;
import org.scijava.Context;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DefaultSegmentationModelTest {

	@Test
	public void testListener() {
		BitType flag = new BitType(false);
		DefaultSegmentationModel model = new DefaultSegmentationModel(
			new DefaultInputImage(ArrayImgs.unsignedBytes(100, 100)), new Context());
		model.listChangeListeners().add(flag::setOne);
		assertFalse(flag.get());
		assertEquals(1, model.segmenters().size());
		model.addSegmenter();
		assertTrue(flag.get());
		assertEquals(2, model.segmenters().size());
		flag.set(false);
		SegmentationItem second = model.segmenters().get(1);
		model.remove(second);
		assertTrue(flag.get());
		assertEquals(1, model.segmenters().size());
	}
}
