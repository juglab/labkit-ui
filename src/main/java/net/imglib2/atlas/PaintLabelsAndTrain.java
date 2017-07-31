package net.imglib2.atlas;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.imglib2.algorithm.features.*;
import net.imglib2.algorithm.features.gui.FeatureSettingsGui;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.cache.img.CellLoader;

import hr.irb.fastRandomForest.FastRandomForest;
import ij.ImagePlus;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.classification.weka.WekaClassifier;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;

import static net.imglib2.algorithm.features.GroupedFeatures.*;
import static net.imglib2.algorithm.features.SingleFeatures.*;

public class PaintLabelsAndTrain
{

	public static void main( final String[] args ) throws IncompatibleTypeException, IOException
	{

		final String imgPath = System.getProperty( "user.home" ) + "/Downloads/epfl-em/training.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128, 2 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );


		final int nLabels = 2;
		final List< String > classLabels = IntStream.range( 0, nLabels ).mapToObj( l -> "class " + l ).collect( Collectors.toList() );

		final ArrayImg< UnsignedByteType, ByteArray > rawData = ArrayImgs.unsignedBytes( dimensions );
		for ( final Pair< UnsignedByteType, UnsignedByteType > p : Views.interval( Views.pair( rawImg, rawData ), rawImg ) )
			p.getB().set( p.getA() );
		final RandomAccessibleInterval< FloatType > converted = Converters.convert( ( RandomAccessibleInterval< UnsignedByteType > ) rawData, new RealFloatConverter<>(), new FloatType() );

		FeatureGroup featureGroup = FeatureSettingsGui.show().orElse(Features.group(SingleFeatures.identity(), GroupedFeatures.gauss()));

		final List<RandomAccessibleInterval<FloatType>> featuresList = featureGroup.features().stream()
				.map(feature -> cachedFeature(converted, grid, feature))
				.collect(Collectors.toList());

		final RandomAccessibleInterval< FloatType > features = Views.concatenate( 3, featuresList );

		final FastRandomForest wekaClassifier = new FastRandomForest();

		final TrainableSegmentationClassifier<ShortType> classifier = new TrainableSegmentationClassifier<>(wekaClassifier, classLabels, featureGroup);

		new MainFrame().trainClassifier( rawData, featuresList, classifier, nLabels, grid, true);
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

}
