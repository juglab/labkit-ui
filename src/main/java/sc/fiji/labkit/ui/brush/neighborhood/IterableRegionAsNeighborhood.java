/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.ui.brush.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

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
