
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
