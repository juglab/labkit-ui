package net.imglib2.atlas;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class FeatureStack {

	private FeatureGroup filter = null;

	private RandomAccessibleInterval<?> original;

	private List<RandomAccessibleInterval<FloatType>> slices;

	private RandomAccessibleInterval<FloatType> block;

	private List<RandomAccessibleInterval<FloatType>> perFilter;

	private CellGrid grid;

	private Notifier<Runnable> listeners = new Notifier<>();

	public FeatureStack(RandomAccessibleInterval<?> original, Classifier classifier, CellGrid grid) {
		this.original = original;
		this.grid = grid;
		classifier.listeners().add((c, ignored) -> setFilter(c.features()));
		setFilter(classifier.features());
	}

	public void setFilter(FeatureGroup featureGroup) {
		if(filter != null && filter.equals(featureGroup))
			return;
		filter = featureGroup;
		int nDim = original.numDimensions();
		List<RandomAccessible<FloatType>> extendedOriginal = prepareOriginal(original);
		System.out.println("Channel Number in Original" + extendedOriginal.size());
		perFilter = featureGroup.features().stream()
				.map(feature -> cachedFeature(feature, extendedOriginal))
				.collect(Collectors.toList());
		slices = perFilter.stream().flatMap(feature ->
				feature.numDimensions() == nDim ?
						Stream.of(feature) :
						RevampUtils.slices(feature).stream()
		).collect(Collectors.toList());
		block = Views.stack(slices);
		listeners.forEach(Runnable::run);
	}

	private List<RandomAccessible<FloatType>> prepareOriginal(RandomAccessibleInterval<?> original) {
		Object voxel = original.randomAccess().get();
		if(voxel instanceof RealType)
			return Collections.singletonList(Views.extendBorder(AtlasUtils.toFloat((RandomAccessibleInterval<RealType<?>>)original)));
		if(voxel instanceof ARGBType)
			return RevampUtils.splitChannels((RandomAccessibleInterval<ARGBType>) original)
					.stream().map(Views::extendBorder).collect(Collectors.toList());
		throw new IllegalArgumentException("original must be a RandomAccessibleInterval of FloatType or ARGBType");
	}

	private Img<FloatType> cachedFeature(FeatureOp feature, List<RandomAccessible<FloatType>> extendedOriginal) {
		int count = feature.count();
		if(count <= 0)
			throw new IllegalArgumentException();
		long[] dimensions = AtlasUtils.extend(grid.getImgDimensions(), count);
		int[] cellDimensions = AtlasUtils.extend(new int[grid.numDimensions()], count);
		grid.cellDimensions(cellDimensions);
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( false );
		final DiskCachedCellImgFactory< FloatType > featureFactory = new DiskCachedCellImgFactory<>( featureOpts );
		CellLoader<FloatType> loader = target -> feature.apply(extendedOriginal, RevampUtils.slices(target));
		return featureFactory.create(dimensions, new FloatType(), loader);
	}

	public RandomAccessibleInterval<FloatType> block() {
		return block;
	}

	public List<RandomAccessibleInterval<FloatType>> slices() {
		return slices;
	}

	public FeatureGroup filter() {
		return filter;
	}

	public Interval interval() {
		return new FinalInterval(original);
	}

	public CellGrid grid() {
		return grid;
	}

	public Notifier<Runnable> listeners() {
		return listeners;
	}
}
