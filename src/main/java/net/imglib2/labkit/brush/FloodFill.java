
package net.imglib2.labkit.brush;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FloodFill {

	public static <T extends Type<T>> void cachedFloodFill(
		RandomAccessibleInterval<T> image, Localizable seed,
		Predicate<? super T> visit, Consumer<? super T> operation)
	{
		if (Util.getTypeFromInterval(image) instanceof LabelingType) {
			visit = (Predicate) new CacheForPredicateLabelingType<>(
				(Predicate) visit);
			operation = (Consumer) new CacheForOperationLabelingType<>(
				(Consumer) operation);
		}
		doFloodFill(image, seed, (Predicate) visit, (Consumer) operation);
	}

	public static <T extends Type<T>> void doFloodFill(
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

	public static void doFloodFillOnActiveLabels(
		RandomAccessibleInterval<LabelingType<Label>> labeling, Point seed,
		Consumer<? super LabelingType<Label>> operation)
	{
		Set<Label> seedValue = getPixel(labeling, seed).copy();
		Predicate<LabelingType<Label>> visit = value -> activeLabelsAreEquals(value,
			seedValue);
		cachedFloodFill(labeling, seed, visit, operation);
	}

	private static boolean activeLabelsAreEquals(LabelingType<Label> a,
		Set<Label> b)
	{
		boolean bIsSubSetOfA = b.stream().filter(Label::isActive).allMatch(
			a::contains);
		boolean aIsSubSetOfB = a.stream().filter(Label::isActive).allMatch(
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

	private static class CacheForPredicateLabelingType<T> implements
		Predicate<LabelingType<T>>
	{

		private final Predicate<LabelingType<T>> predicate;
		private final TIntIntMap cache = new TIntIntHashMap();
		private final int noEntryValue = cache.getNoEntryValue();

		CacheForPredicateLabelingType(Predicate<LabelingType<T>> predicate) {
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

		private final Consumer<LabelingType<T>> operation;

		private final TIntIntMap cache = new TIntIntHashMap();
		private final int noEntryValue = cache.getNoEntryValue();

		private CacheForOperationLabelingType(Consumer<LabelingType<T>> operation) {
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
