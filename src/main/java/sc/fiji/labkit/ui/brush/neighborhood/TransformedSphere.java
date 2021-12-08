
package sc.fiji.labkit.ui.brush.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;

public class TransformedSphere {

	private final AffineTransform3D transform;

	public TransformedSphere(AffineTransform3D transform) {
		this.transform = transform;
	}

	public boolean contains(RealLocalizable point) {
		RealPoint out = new RealPoint(3);
		transform.applyInverse(out, point);
		return RealPoints.squaredLength(out) <= 1;
	}

	public RealInterval realBoundingBox() {
		double[] min = new double[3];
		double[] max = new double[3];
		for (int d = 0; d < 3; d++) {
			double halfLength = Math.abs(transform.get(d, 0)) + Math.abs(transform
				.get(d, 1)) + Math.abs(transform.get(d, 2));
			double center = transform.get(d, 3);
			min[d] = center - halfLength;
			max[d] = center + halfLength;
		}
		return new FinalRealInterval(min, max);
	}

	public Interval boundingBox() {
		RealInterval boundingBox = realBoundingBox();
		long[] min = new long[boundingBox.numDimensions()];
		long[] max = new long[boundingBox.numDimensions()];
		for (int d = 0; d < boundingBox.numDimensions(); d++) {
			min[d] = (long) Math.floor(boundingBox.realMin(d));
			max[d] = (long) Math.ceil(boundingBox.realMax(d));
		}
		return new FinalInterval(min, max);
	}

	// static methods

	public static <T> IterableRegionAsNeighborhood<T> asNeighborhood(
		long position[], AffineTransform3D transformation,
		final RandomAccess<T> source)
	{
		TransformedSphere sphere = new TransformedSphere(transformation);
		IterableRegion<BitType> region = iterableRegion(sphere, source
			.numDimensions());
		IterableRegionAsNeighborhood<T> neighborhood =
			new IterableRegionAsNeighborhood<>(region, source);
		neighborhood.setPosition(position);
		return neighborhood;
	}

	static IterableRegion<BitType> iterableRegion(TransformedSphere sphere,
		int numDimensions)
	{
		return iterableRegion(sphere, intervalChangeNumDimensions(sphere
			.boundingBox(), numDimensions));
	}

	private static Interval intervalChangeNumDimensions(final Interval interval,
		int numDimensions)
	{
		long[] min = Arrays.copyOf(Intervals.minAsLongArray(interval),
			numDimensions);
		long[] max = Arrays.copyOf(Intervals.maxAsLongArray(interval),
			numDimensions);
		return new FinalInterval(min, max);
	}

	private static IterableRegion<BitType> iterableRegion(
		TransformedSphere sphere, Interval interval)
	{
		SparseIterableRegion result = new SparseIterableRegion(interval);
		Cursor<BitType> cursor = Views.flatIterable(adoptToDimension(result, 3))
			.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().set(sphere.contains(cursor));
		}
		return result;
	}

	private static <T> RandomAccessibleInterval<T> adoptToDimension(
		RandomAccessibleInterval<T> result, int numDimension)
	{
		while (result.numDimensions() < numDimension)
			result = Views.addDimension(result, 0, 0);
		if (result.numDimensions() > numDimension)
			throw new UnsupportedOperationException();
		return result;
	}
}
