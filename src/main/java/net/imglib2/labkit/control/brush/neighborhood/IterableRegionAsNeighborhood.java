
package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;

import java.util.Arrays;
import java.util.Iterator;

/**
 * TODO
 *
 * @author Tobias Pietzsch
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 */
public class IterableRegionAsNeighborhood<T> extends Point implements
	Neighborhood<T>
{

	private final RandomAccess<T> source;

	private final IterableRegion<BitType> region;

	public IterableRegionAsNeighborhood(IterableRegion<BitType> region,
		final RandomAccess<T> source)
	{
		super(source.numDimensions());
		this.source = source;
		this.region = region;
	}

	@Override
	public Interval getStructuringElementBoundingBox() {
		return region;
	}

	@Override
	public Cursor<T> cursor() {
		return new MappingCursor<>(position, region.cursor(), source
			.copyRandomAccess());
	}

	@Override
	public Cursor<T> localizingCursor() {
		return cursor();
	}

	@Override
	public long size() {
		return region.size();
	}

	@Override
	public T firstElement() {
		return cursor().next();
	}

	@Override
	public Object iterationOrder() {
		return this;
	}

	@Override
	public Iterator<T> iterator() {
		return cursor();
	}

	@Override
	public long min(int d) {
		return region.min(d) + position[d];
	}

	@Override
	public void min(long[] min) {
		for (int d = 0; d < numDimensions(); d++)
			min[d] = min(d);
	}

	@Override
	public void min(Positionable min) {
		for (int d = 0; d < numDimensions(); d++)
			min.setPosition(min(d), d);
	}

	@Override
	public long max(int d) {
		return region.max(d) + position[d];
	}

	@Override
	public void max(long[] max) {
		for (int d = 0; d < numDimensions(); d++)
			max[d] = max(d);
	}

	@Override
	public void max(Positionable max) {
		for (int d = 0; d < numDimensions(); d++)
			max.setPosition(max(d), d);
	}

	@Override
	public void dimensions(long[] dimensions) {
		region.dimensions(dimensions);
	}

	@Override
	public long dimension(int d) {
		return region.dimension(d);
	}

	@Override
	public double realMin(int d) {
		return region.realMin(d) + position[d];
	}

	@Override
	public void realMin(double[] min) {
		for (int d = 0; d < numDimensions(); d++)
			min[d] = realMin(d);
	}

	@Override
	public void realMin(RealPositionable min) {
		for (int d = 0; d < numDimensions(); d++)
			min.setPosition(realMin(d), d);
	}

	@Override
	public double realMax(int d) {
		return region.realMax(d) + position[d];
	}

	@Override
	public void realMax(double[] max) {
		for (int d = 0; d < numDimensions(); d++)
			max[d] = realMax(d);
	}

	@Override
	public void realMax(RealPositionable max) {
		for (int d = 0; d < numDimensions(); d++)
			max.setPosition(realMax(d), d);
	}

}
