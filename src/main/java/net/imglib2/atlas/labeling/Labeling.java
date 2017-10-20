package net.imglib2.atlas.labeling;

import com.google.gson.annotations.JsonAdapter;
import net.imglib2.*;
import net.imglib2.RandomAccess;
import net.imglib2.atlas.AtlasUtils;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.sparse.SparseRandomAccessIntType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.util.*;

/**
 * @author Matthias Arzt
 */
@JsonAdapter(LabelingSerializer.Adapter.class)
public class Labeling extends AbstractWrappedInterval implements RandomAccessibleInterval<Set<String>> {

	private final ImgLabeling<String, ?> imgLabeling;
	private List<String> labels;

	public Labeling(Map<String,IterableRegion<BitType>> regions, Interval interval) {
		this(initImgLabling(regions, interval));
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

	public Labeling(ImgLabeling<String, ?> labeling) {
		super(labeling);
		imgLabeling = labeling;
		labels = new ArrayList<>(labeling.getMapping().getLabels());
	}

	public Labeling(List<String> labels, Interval interval) {
		this(new ImgLabeling<>(new SparseRandomAccessIntType(interval)));
		this.labels = labels;
	}


	public List<String> getLabels() {
		return labels;
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

	private Cursor<?> sparsityCursor() {
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
		return AtlasUtils.uncheckedCast(imgLabeling.randomAccess());
	}

	@Override
	public RandomAccess<Set<String>> randomAccess(Interval interval) {
		return AtlasUtils.uncheckedCast(imgLabeling.randomAccess(interval));
	}
}
