
package demo.custom_segmenter;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Cast;

class MeanCalculator {

	private double sum = 0;
	private long count = 0;

	public void addSample(RandomAccessibleInterval<?> image,
		IterableRegion<BitType> region)
	{
		Cursor<Void> cursor = region.cursor();
		RandomAccess<RealType<?>> randomAccess = Cast.unchecked(image.randomAccess());
		while (cursor.hasNext()) {
			cursor.fwd();
			randomAccess.setPosition(cursor);
			sum += randomAccess.get().getRealDouble();
			count++;
		}
	}

	public double mean() {
		return sum / count;
	}
}
