package net.imglib2.atlas;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class FeatureStack {

	private FeatureGroup filter = null;

	private RandomAccessibleInterval<FloatType> original;

	private List<RandomAccessibleInterval<FloatType>> slices;

	private RandomAccessibleInterval<FloatType> block;

	private List<RandomAccessibleInterval<FloatType>> perFilter;

	private CellGrid grid;

	public FeatureStack(RandomAccessibleInterval<FloatType> original, Classifier classifier, CellGrid grid) {
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
		perFilter = featureGroup.features().stream()
				.map(feature -> cachedFeature(original, grid, feature))
				.collect(Collectors.toList());
		slices = perFilter.stream().flatMap(feature ->
				feature.numDimensions() == nDim ?
						Stream.of(feature) :
						RevampUtils.slices(feature).stream()
		).collect(Collectors.toList());
		block = Views.stack(slices);
	}

	private static Img<FloatType> cachedFeature(RandomAccessibleInterval<FloatType> original, CellGrid grid, Feature feature) {
		int count = feature.count();
		if(count <= 0)
			throw new IllegalArgumentException();
		long[] dimensions = AtlasUtils.extend(Intervals.dimensionsAsLongArray(original), count);
		int[] cellDimensions = AtlasUtils.extend(new int[grid.numDimensions()], count);
		grid.cellDimensions(cellDimensions);
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( false );
		final DiskCachedCellImgFactory< FloatType > featureFactory = new DiskCachedCellImgFactory<>( featureOpts );
		RandomAccessible<FloatType> extendedOriginal = Views.extendBorder(original);
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
}
