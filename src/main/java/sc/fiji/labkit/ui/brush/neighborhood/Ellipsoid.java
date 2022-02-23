
package sc.fiji.labkit.ui.brush.neighborhood;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;

import java.util.Arrays;

import static java.lang.Math.sqrt;

/**
 * This class creates an iterable region that is shaped like an ellipse,
 * ellipsoid or hyper ellipsoid.
 */
public class Ellipsoid {

	/**
	 * @return an iterable region that is shaped like an ellipse or ellipsoid. The
	 *         returned iterable region contains all pixels that intersect with the
	 *         ellipse / ellipsoid. A pixel itself is considered to be a square of
	 *         size 1x1 centered around the pixel coordinates. (Voxel are handled
	 *         similarly).
	 */
	public static IterableRegion<BitType> asIterableRegion(double[] center, double[] axes) {
		return new Ellipsoid(center, axes).asIterableRegion();
	}

	private final double[] center;

	private final double[] axes;

	Ellipsoid(double[] center, double[] axes) {
		if (center.length != axes.length)
			throw new IllegalArgumentException("Ellipsoid center and size must" +
				" have the same number of dimensions: " +
				"center: " + Arrays.toString(center) +
				" sizes: " + Arrays.toString(axes));
		this.center = center;
		this.axes = axes;
	}

	IterableRegion<BitType> asIterableRegion() {

		SparseIterableRegion sparseIterableRegion = new SparseIterableRegion(boundingBox());
		RandomAccess<BitType> ra = sparseIterableRegion.randomAccess();
		addPoints(boundingBox().numDimensions() - 1, ra, 1);
		return sparseIterableRegion;
	}

	private void addPoints(int d, RandomAccess<BitType> randomAccess, double squaredScaleFactor) {
		double c = center[d];
		double scaleFactor = sqrt(squaredScaleFactor);
		long min = roundDown(c - axes[d] * scaleFactor);
		long center_min = roundDown(c);
		long center_max = roundUp(c);
		long max = roundUp(c + axes[d] * scaleFactor);
		if (d == 0) {
			randomAccess.setPosition(min, 0);
			for (long x = min; x <= max; x++) {
				randomAccess.get().set(true);
				randomAccess.fwd(0);
			}
		}
		else {
			for (long x = min; x < center_min; x++) {
				randomAccess.setPosition(x, d);
				addPoints(d - 1, randomAccess, squaredScaleFactor - sqr((c - x - 0.5) / axes[d]));
			}
			for (long x = center_min; x <= center_max; x++) {
				randomAccess.setPosition(x, d);
				addPoints(d - 1, randomAccess, squaredScaleFactor);
			}
			for (long x = center_max + 1; x <= max; x++) {
				randomAccess.setPosition(x, d);
				addPoints(d - 1, randomAccess, squaredScaleFactor - sqr((x - c - 0.5) / axes[d]));
			}
		}
	}

	private double sqr(double v) {
		return v * v;
	}

	Interval boundingBox() {
		int n = center.length;
		long[] min = new long[n];
		for (int d = 0; d < n; d++)
			min[d] = roundDown(center[d] - axes[d]);
		long[] max = new long[n];
		for (int d = 0; d < n; d++)
			max[d] = roundUp(center[d] + axes[d]);
		return new FinalInterval(min, max);
	}

	static long roundUp(double v) {
		return Math.round(v);
	}

	static long roundDown(double v) {
		return -Math.round(-v);
	}

}
