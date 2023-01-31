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

package sc.fiji.labkit.ui.brush;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class FloodFillTest {

	private static long[] size = { 8, 5 };
	Interval intervalA = Intervals.createMinSize(1, 0, 3, 4);
	Interval intervalB = Intervals.createMinSize(1, 0, 6, 3);
	Interval intervalC = Intervals.createMinSize(1, 1, 3, 3);
	Interval intervalAintersectB = Intervals.createMinSize(1, 0, 3, 3);

	private RandomAccessibleInterval<BitType> imageA = asBits(intervalA, size);
	private RandomAccessibleInterval<BitType> imageB = asBits(intervalB, size);
	private RandomAccessibleInterval<BitType> expectedComponent = asBits(
		intervalAintersectB, size);

	private RandomAccessibleInterval<BitType> asBits(Interval ones,
		long... dims)
	{
		RandomAccessibleInterval<BitType> result = ArrayImgs.bits(dims);
		Views.interval(result, ones).forEach(BitType::setOne);
		return result;
	}

	@Test
	public void test2() {
		RandomAccessibleInterval<LabelingType<String>> labeling =
			exampleImgLabeling();
		Predicate<LabelingType<String>> visit = set -> set.contains("a") && set
			.contains("b") && !set.contains("ab");
		final Point seed = new Point(2, 2);
		FloodFill.cachedFloodFill(labeling, seed, visit, l -> l.add("ab"));
		assertLabelEqualsInterval(labeling, intervalA, "a");
		assertLabelEqualsInterval(labeling, intervalB, "b");
		assertLabelEqualsInterval(labeling, intervalC, "c");
		assertLabelEqualsInterval(labeling, intervalAintersectB, "ab");
	}

	public ImgLabeling<String, ?> exampleImgLabeling() {
		ImgLabeling<String, ?> labeling = new ImgLabeling<>(ArrayImgs.ints(size));
		fillIntervalWithLabel(labeling, intervalA, "a");
		fillIntervalWithLabel(labeling, intervalB, "b");
		fillIntervalWithLabel(labeling, intervalC, "c");
		return labeling;
	}

	@Test
	public void test3() {
		Labeling labeling = Labeling.fromImgLabeling(exampleImgLabeling());
		final Point seed = new Point(2, 2);
		Label a = labeling.getLabel("a");
		Label b = labeling.getLabel("b");
		Label c = labeling.getLabel("c");
		c.setVisible(false);
		Label ab = labeling.addLabel("ab");
		final Consumer<Set<Label>> operation = l -> l.add(ab);
		FloodFill.doFloodFillOnActiveLabels((RandomAccessibleInterval) labeling,
			seed, operation);
		assertLabelEqualsInterval(labeling, intervalA, a);
		assertLabelEqualsInterval(labeling, intervalB, b);
		assertLabelEqualsInterval(labeling, intervalC, c);
		assertLabelEqualsInterval(labeling, intervalAintersectB, ab);
	}

	private <T> void assertLabelEqualsInterval(
		RandomAccessibleInterval<? extends Set<T>> labeling, Interval interval,
		T label)
	{
		Cursor<? extends Set<T>> cursor = Views.iterable(labeling)
			.localizingCursor();
		while (cursor.hasNext()) {
			Set<T> value = cursor.next();
			final boolean expected = Intervals.contains(interval, cursor);
			final boolean actual = value.contains(label);
			assertEquals(expected, actual);
		}
	}

	public void fillIntervalWithLabel(
		RandomAccessibleInterval<LabelingType<String>> labeling, Interval interval,
		String label)
	{
		Views.interval(labeling, interval).forEach(l -> l.add(label));
	}
}
