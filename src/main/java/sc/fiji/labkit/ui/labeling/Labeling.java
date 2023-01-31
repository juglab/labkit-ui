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

package sc.fiji.labkit.ui.labeling;

import com.google.gson.annotations.JsonAdapter;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.*;
import net.imglib2.RandomAccess;
import sc.fiji.labkit.ui.utils.ColorSupplier;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;
import sc.fiji.labkit.ui.utils.sparse.SparseRandomAccessIntType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Cast;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@link Labeling} is basically an {@code ImgLabeling<Label, ?>} with some
 * additional functionality.
 *
 * @author Matthias Arzt
 */
@JsonAdapter(LabelingSerializer.Adapter.class)
public class Labeling extends AbstractWrappedInterval<Interval> implements
	RandomAccessibleInterval<LabelingType<Label>>
{

	private final ImgLabeling<Label, ?> imgLabeling;
	private List<Label> labels;
	private List<CalibratedAxis> axes;
	private ColorSupplier colorSupplier;

	public static Labeling createEmpty(List<String> labels, Interval interval) {
		Labeling result = createEmptyLabels(Collections.emptyList(), interval);
		labels.forEach(result::addLabel);
		return result;
	}

	public static Labeling createEmptyLabels(List<Label> labels,
		Interval interval)
	{
		final ImgLabeling<Label, IntType> imgLabeling = new ImgLabeling<>(
			new SparseRandomAccessIntType(interval));
		return new Labeling(labels, imgLabeling, new ColorSupplier());
	}

	public static Labeling fromImgLabeling(ImgLabeling<String, ?> imgLabeling) {
		ColorSupplier colors = new ColorSupplier();
		ImgLabeling<Label, ?> labelsImgLabeling = Labelings.mapLabels(imgLabeling,
			name -> new Label(name, colors.get()));
		return new Labeling(new ArrayList<>(labelsImgLabeling.getMapping()
			.getLabels()), labelsImgLabeling, colors);
	}

	public static Labeling fromImg(RandomAccessibleInterval<? extends IntegerType<?>> img) {
		ColorSupplier colors = new ColorSupplier();
		TIntSet values = new TIntHashSet(100, 0.5f, 0);
		LoopBuilder.setImages(img).forEachPixel(x -> {
			int integer = x.getInteger();
			if (integer != 0)
				values.add(integer);
		});
		int max = IntStream.of(values.toArray()).max().orElse(0);
		List<Label> allLabels = new ArrayList<>(max);
		List<Label> nonEmptyLabels = new ArrayList<>(values.size());
		for (int i = 1; i <= max; i++) {
			Label label = new Label(Integer.toString(i), new ARGBType(0xffffff));
			if (values.contains(i)) {
				nonEmptyLabels.add(label);
				label.setColor(colors.get());
			}
			allLabels.add(label);
		}
		ImgLabeling<Label, ?> imgLabeling = ImgLabeling.fromImageAndLabels(Cast.unchecked(img),
			allLabels);
		return new Labeling(nonEmptyLabels, imgLabeling, colors);
	}

	public static Labeling fromMap(Map<String, IterableRegion<BitType>> regions) {
		if (regions.isEmpty()) throw new IllegalArgumentException(
			"Labeling.fromMap: The given map must not be empty.");
		ColorSupplier colors = new ColorSupplier();
		Map<Label, IterableRegion<BitType>> regions2 = new LinkedHashMap<>();
		for (Map.Entry<String, IterableRegion<BitType>> entry : regions.entrySet())
			regions2.put(new Label(entry.getKey(), colors.get()), entry.getValue());
		final ArrayList<Label> labels = new ArrayList<>(regions2.keySet());
		final ImgLabeling<Label, ?> imgLabling = initImgLabling(regions2);
		return new Labeling(labels, imgLabling, colors);
	}

	private static ImgLabeling<Label, ?> initImgLabling(
		Map<Label, IterableRegion<BitType>> regions)
	{
		Interval interval = getInterval(regions.values());
		ImgLabeling<Label, ?> imgLabeling = new ImgLabeling<>(
			new SparseRandomAccessIntType(interval));
		RandomAccess<LabelingType<Label>> ra = imgLabeling.randomAccess();
		regions.forEach((label, region) -> {
			Cursor<Void> cursor = region.cursor();
			while (cursor.hasNext()) {
				cursor.fwd();
				ra.setPosition(cursor);
				ra.get().add(label);
			}
		});
		return imgLabeling;
	}

	private static Interval getInterval(
		Collection<? extends Interval> intervals)
	{
		Interval result = new FinalInterval(intervals.iterator().next());
		for (Interval interval : intervals)
			if (!Intervals.equals(result, interval))
				throw new IllegalArgumentException("Intervals must match");
		return result;
	}

	private Labeling(List<Label> labels, ImgLabeling<Label, ?> labeling,
		ColorSupplier colorSupplier)
	{
		super(labeling);
		this.imgLabeling = labeling;
		this.labels = new ArrayList<>(labels);
		this.colorSupplier = colorSupplier;
		this.axes = initAxes(labeling.numDimensions());
	}

	private List<CalibratedAxis> initAxes(int i) {
		return IntStream.range(0, i).mapToObj(ignore -> new DefaultLinearAxis())
			.collect(Collectors.toList());
	}

	public Interval interval() {
		return new FinalInterval(imgLabeling);
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setAxes(List<CalibratedAxis> axes) {
		this.axes = axes.stream().map(CalibratedAxis::copy).collect(Collectors
			.toList());
	}

	public Label getLabel(String name) {
		for (Label label : labels) {
			if (label.name().equals(name)) return label;
		}
		throw new NoSuchElementException();
	}

	public RandomAccessibleInterval<BitType> getRegion(Label label) {
		return slice(imgLabeling, label);
	}

	public Map<Label, IterableRegion<BitType>> iterableRegions() {
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<LabelingType<Label>> ra = imgLabeling.randomAccess();
		Map<Label, SparseIterableRegion> regions = new HashMap<>();
		labels.forEach(label -> regions.put(label, new SparseIterableRegion(
			imgLabeling)));
		while (cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			ra.get().forEach(label -> regions.get(label).add(cursor));
		}
		return Collections.unmodifiableMap(regions);
	}

	public Cursor<?> sparsityCursor() {
		RandomAccessibleInterval<?> indexImg = imgLabeling.getIndexImg();
		if (indexImg instanceof SparseRandomAccessIntType)
			return ((SparseRandomAccessIntType) indexImg).sparseCursor();
		else {
			RandomAccessible<Void> voids = ConstantUtils.constantRandomAccessible(
				null, imgLabeling.numDimensions());
			return Views.interval(voids, imgLabeling).cursor();
		}
	}

	private static <T> RandomAccessibleInterval<BitType> slice(
		RandomAccessibleInterval<? extends Set<T>> labeling, T value)
	{
		Converter<Set<T>, BitType> converter = (in, out) -> {
			@SuppressWarnings("unchecked")
			SetEntryAsBitType<T> modifyingBitType = ((SetEntryAsBitType<T>) out);
			modifyingBitType.setSet(in);
		};
		return Converters.convert(labeling, converter, new SetEntryAsBitType<>(
			value));
	}

	public RandomAccessibleInterval<? extends IntegerType<?>> getIndexImg() {
		return imgLabeling.getIndexImg();
	}

	public List<Set<Label>> getLabelSets() {
		LabelingMapping<Label> mapping = imgLabeling.getMapping();
		return new AbstractList<Set<Label>>() {

			@Override
			public Set<Label> get(int index) {
				return mapping.labelsAtIndex(index);
			}

			@Override
			public int size() {
				return mapping.numSets();
			}
		};
	}

	public List<CalibratedAxis> axes() {
		return axes;
	}

	public Label addLabel(String label) {
		Objects.requireNonNull(label);
		final Label e = new Label(label, colorSupplier.get());
		labels.add(e);
		return e;
	}

	public void addLabel(String newName,
		RandomAccessibleInterval<? extends BooleanType<?>> bitmap)
	{
		Label label = addLabel(newName);
		LoopBuilder.setImages(bitmap, this).forEachPixel((i, o) -> {
			if (i.get()) o.add(label);
		});
	}

	public void removeLabel(Label label) {
		if (!labels.contains(label)) return;
		labels.remove(label);
		clearLabel(label);
	}

	public void renameLabel(Label oldLabel, String newLabel) {
		oldLabel.setName(newLabel);
	}

	public void clearLabel(Label label) {
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<LabelingType<Label>> ra = randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			Set<Label> set = ra.get();
			set.remove(label);
		}
	}

	public void setLabelOrder(Comparator<? super Label> comparator) {
		labels.sort(comparator);
	}

	public static class SetEntryAsBitType<T> extends BitType {

		private Set<T> set = null;
		private final T entry;

		public SetEntryAsBitType(T entry) {
			this.entry = entry;
		}

		public void setSet(Set<T> set) {
			this.set = set;
		}

		@Override
		public BitType createVariable() {
			return copy();
		}

		@Override
		public BitType copy() {
			return new SetEntryAsBitType<>(entry);
		}

		@Override
		public boolean get() {
			if (set == null) return false;
			return set.contains(entry);
		}

		@Override
		public void set(boolean value) {
			if (set == null) return;
			if (value) set.add(entry);
			else set.remove(entry);
		}
	}

	@Override
	public RandomAccess<LabelingType<Label>> randomAccess() {
		return Cast.unchecked(imgLabeling.randomAccess());
	}

	@Override
	public RandomAccess<LabelingType<Label>> randomAccess(Interval interval) {
		return Cast.unchecked(imgLabeling.randomAccess(interval));
	}
}
