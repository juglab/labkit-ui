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

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import sc.fiji.labkit.ui.labeling.Label;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Helper for {@link FloodFillController}.
 */
class FloodFill {

	/**
	 * Does a flood fill on the given labeling, starting from the seed point. Only
	 * the visible labels are taken into account. Fills in all the pixels that have
	 * exactly the same visible labels as the seed point.
	 * 
	 * @param labeling Input and output to the flood fill operation.
	 * @param seed Seed point.
	 * @param operation Operation that es performed for the flood filled pixels.
	 */
	public static void doFloodFillOnActiveLabels(
		RandomAccessibleInterval<LabelingType<Label>> labeling, Point seed,
		Consumer<? super LabelingType<Label>> operation)
	{
		Set<Label> seedValue = getPixel(labeling, seed).copy();
		Predicate<LabelingType<Label>> visit = value -> activeLabelsAreEquals(value,
			seedValue);
		cachedFloodFill(labeling, seed, visit, operation);
	}

	// package-private to allow testing
	static <T> void cachedFloodFill(
		RandomAccessibleInterval<LabelingType<T>> image, Localizable seed,
		Predicate<? super LabelingType<T>> visit, Consumer<? super LabelingType<T>> operation)
	{
		Predicate<LabelingType<T>> cachedVisit = new CacheForPredicateLabelingType<>(visit);
		Consumer<LabelingType<T>> cachedOperation = new CacheForOperationLabelingType<>(operation);
		doFloodFill(image, seed, cachedVisit, cachedOperation);
	}

	private static <T extends Type<T>> void doFloodFill(
		RandomAccessibleInterval<T> image, Localizable seed, Predicate<T> visit,
		Consumer<T> operation)
	{
		RandomAccess<T> ra = image.randomAccess();
		ra.setPosition(seed);
		T seedValue = ra.get().copy();
		T seedValueChanged = seedValue.copy();
		operation.accept(seedValueChanged);
		if (visit.test(seedValueChanged)) return;
		BiPredicate<T, T> filter = (f, s) -> visit.test(f);
		ExtendedRandomAccessibleInterval<T, RandomAccessibleInterval<T>> target =
			Views.extendValue(image, seedValueChanged);
		final DiamondShape shape = new DiamondShape(1);
		net.imglib2.algorithm.fill.FloodFill.fill(target, target, seed, shape,
			filter, operation);
	}

	private static boolean activeLabelsAreEquals(LabelingType<Label> a,
		Set<Label> b)
	{
		boolean bIsSubSetOfA = b.stream().filter(Label::isVisible).allMatch(
			a::contains);
		boolean aIsSubSetOfB = a.stream().filter(Label::isVisible).allMatch(
			b::contains);
		return aIsSubSetOfB && bIsSubSetOfA;
	}

	private static <T> T getPixel(RandomAccessible<T> image,
		Localizable position)
	{
		RandomAccess<T> ra = image.randomAccess();
		ra.setPosition(position);
		return ra.get();
	}

	public static boolean isBackgroundFloodFill(RandomAccessibleInterval<LabelingType<Label>> frame,
		Point seed, Consumer<Set<Label>> operation)
	{
		LabelingType<Label> seedValue = frame.randomAccess().setPositionAndGet(seed);
		LabelingType<Label> changedSeedValue = seedValue.copy();
		operation.accept(changedSeedValue);
		long numberOfActiveLabelsBefore = seedValue.stream().filter(Label::isVisible).count();
		long numberOfActiveLabelsAfter = changedSeedValue.stream().filter(Label::isVisible).count();
		boolean isBackgroundFill = numberOfActiveLabelsBefore == 0;
		boolean operationHasEffect = numberOfActiveLabelsAfter > 0;
		return isBackgroundFill && operationHasEffect;
	}

	private static class CacheForPredicateLabelingType<T> implements
		Predicate<LabelingType<T>>
	{

		private final Predicate<? super LabelingType<T>> predicate;
		private final TIntIntMap cache = new TIntIntHashMap();
		private final int noEntryValue = cache.getNoEntryValue();

		CacheForPredicateLabelingType(Predicate<? super LabelingType<T>> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean test(LabelingType<T> ts) {
			final int input = ts.getIndex().getInteger();
			int cached = cache.get(input);
			if (cached == noEntryValue) {
				boolean value = predicate.test(ts);
				cache.put(input, value ? 1 : 0);
				return value;
			}
			return cached == 1;
		}
	}

	private static class CacheForOperationLabelingType<T> implements
		Consumer<LabelingType<T>>
	{

		private final Consumer<? super LabelingType<T>> operation;

		private final TIntIntMap cache = new TIntIntHashMap();
		private final int noEntryValue = cache.getNoEntryValue();

		private CacheForOperationLabelingType(Consumer<? super LabelingType<T>> operation) {
			this.operation = operation;
		}

		@Override
		public void accept(LabelingType<T> value) {
			final IntegerType<?> valueIndex = value.getIndex();
			final int input = valueIndex.getInteger();
			int cached = cache.get(input);
			if (cached == noEntryValue) {
				operation.accept(value);
				cache.put(input, valueIndex.getInteger());
			}
			else valueIndex.setInteger(cached);
		}
	}
}
