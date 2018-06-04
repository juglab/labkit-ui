package net.imglib2.labkit.labeling;

import com.google.gson.annotations.JsonAdapter;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Matthias Arzt
 */
@JsonAdapter(LabelingSerializer.Adapter.class)
public class Labeling extends AbstractWrappedInterval implements RandomAccessibleInterval<Set<String>> {

	private final ImgLabeling<String, ?> imgLabeling;
	private List<String> labels;
	private List<CalibratedAxis> axes;

	public Labeling(Map<String,IterableRegion<BitType>> regions, Interval interval) {
		this(new ArrayList<>(regions.keySet()), initImgLabling(regions, interval));
	}

	public Labeling(ImgLabeling<String, ?> imgLabeling) {
		this(new ArrayList<>(imgLabeling.getMapping().getLabels()), imgLabeling);
	}

	private static ImgLabeling<String, ?> initImgLabling(Map<String, IterableRegion<BitType>> regions, Interval interval) {
		ImgLabeling<String, ?> imgLabeling = new ImgLabeling<>(new SparseRandomAccessIntType(interval));
		RandomAccess<LabelingType<String>> ra = imgLabeling.randomAccess();
		regions.forEach((label, region) -> {
			Cursor<Void> cursor = region.cursor();
			while(cursor.hasNext()) {
				cursor.fwd();
				ra.setPosition(cursor);
				ra.get().add(label);
			}
		});
		return imgLabeling;
	}

	public Labeling(List<String> labels, ImgLabeling<String, ?> labeling) {
		super(labeling);
		this.imgLabeling = labeling;
		this.labels = new ArrayList<>(labels);
		this.axes = initAxes(labeling.numDimensions());
	}

	private List<CalibratedAxis> initAxes(int i) {
		return IntStream.range(0, i).mapToObj(ignore -> new DefaultLinearAxis()).collect(Collectors.toList());
	}

	public Labeling(List<String> labels, Interval interval) {
		this(labels, new ImgLabeling<>(new SparseRandomAccessIntType(interval)));
	}

	public Interval interval() {
		return new FinalInterval( imgLabeling );
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setAxes(List<CalibratedAxis> axes) {
		this.axes = axes.stream().map(CalibratedAxis::copy).collect(Collectors.toList());
	}

	public Map<String, RandomAccessibleInterval<BitType>> regions() {
		TreeMap<String, RandomAccessibleInterval<BitType>> map = new TreeMap<>();
		labels.forEach(label -> map.put(label, slice(imgLabeling, label)));
		return map;
	}

	public Map<String, IterableRegion<BitType>> iterableRegions() {
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<LabelingType<String>> ra = imgLabeling.randomAccess();
		Map<String, SparseIterableRegion> regions = new TreeMap<>();
		labels.forEach(label -> regions.put(label, new SparseIterableRegion(imgLabeling)));
		while(cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			ra.get().forEach(label -> regions.get(label).add(cursor));
		}
		return Collections.unmodifiableMap(regions);
	}

	public Cursor<?> sparsityCursor() {
		RandomAccessibleInterval<?> indexImg = imgLabeling.getIndexImg();
		if(indexImg instanceof SparseRandomAccessIntType)
			return ((SparseRandomAccessIntType) indexImg).sparseCursor();
		else {
			RandomAccessible<Void> voids = ConstantUtils.constantRandomAccessible(null,
					imgLabeling.numDimensions());
			return Views.interval(voids, imgLabeling).cursor();
		}
	}

	public static <T> RandomAccessibleInterval<BitType> slice(RandomAccessibleInterval<? extends Set<T>> labeling, T value) {
		Converter<Set<T>, BitType> converter = (in, out) -> {
			@SuppressWarnings("unchecked")
			SetEntryAsBitType<T> modifyingBitType = ((SetEntryAsBitType<T>) out);
			modifyingBitType.setSet(in);
		};
		return Converters.convert(labeling, converter, new SetEntryAsBitType<>(value));
	}

	public RandomAccessibleInterval<? extends IntegerType<?>> getIndexImg() {
		return imgLabeling.getIndexImg();
	}

	public List<Set<String>> getLabelSets() {
		LabelingMapping<String> mapping = imgLabeling.getMapping();
		return new AbstractList<Set<String>>() {
			@Override
			public Set<String> get(int index) {
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

	public void addLabel(String label) {
		Objects.requireNonNull(label);
		if(labels.contains(label))
			return;
		labels.add(label);
	}

	public void addLabel( String newName, RandomAccessibleInterval<? extends BooleanType<?>> bitmap )
	{
		addLabel(newName);
		LoopBuilder.setImages( bitmap, this).forEachPixel( (i,o) -> {
			if(i.get()) o.add(newName);
		} );
	}

	public void removeLabel(String label) {
		if(!labels.contains(label))
			return;
		labels.remove(label);
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<Set<String>> ra = randomAccess();
		while(cursor.hasNext())	{
			cursor.fwd();
			ra.setPosition(cursor);
			ra.get().remove(label);
		}
	}

	public void renameLabel(String oldLabel, String newLabel) {
		int index = labels.indexOf(oldLabel);
		if(index < 0)
			return;
		labels.set(index, newLabel);
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<Set<String>> ra = randomAccess();
		while(cursor.hasNext())	{
			cursor.fwd();
			ra.setPosition(cursor);
			Set<String> set = ra.get();
			if(set.remove(oldLabel))
				set.add( newLabel );
		}
	}

    public void clearLabel(String selected) {
		Cursor<?> cursor = sparsityCursor();
		RandomAccess<Set<String>> ra = randomAccess();
		while(cursor.hasNext())	{
			cursor.fwd();
			ra.setPosition(cursor);
			Set<String> set = ra.get();
			set.remove(selected);
		}
    }

	public void moveLabel(String selected, int movement) {
		int oldindex = labels.indexOf(selected);
		int newindex = Math.max(0, Math.min(oldindex + movement, labels.size()-1));
		labels.set(oldindex, labels.get(newindex));
		labels.set(newindex, selected);
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
			if(set == null) return false;
			return set.contains(entry);
		}

		@Override
		public void set(boolean value) {
			if(set == null) return;
			if(value) set.add(entry);
			else set.remove(entry);
		}
	}

	@Override
	public RandomAccess<Set<String>> randomAccess() {
		return LabkitUtils.uncheckedCast(imgLabeling.randomAccess());
	}

	@Override
	public RandomAccess<Set<String>> randomAccess(Interval interval) {
		return LabkitUtils.uncheckedCast(imgLabeling.randomAccess(interval));
	}
}
