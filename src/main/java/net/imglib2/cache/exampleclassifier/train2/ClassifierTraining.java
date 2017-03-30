package net.imglib2.cache.exampleclassifier.train2;

import static bdv.viewer.DisplayMode.SINGLE;
import static net.imglib2.cache.img.AccessFlags.DIRTY;
import static net.imglib2.cache.img.AccessFlags.VOLATILE;
import static net.imglib2.cache.img.PrimitiveType.FLOAT;
import static net.imglib2.cache.img.PrimitiveType.SHORT;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.UncheckedCache;
import net.imglib2.cache.img.AccessIo;
import net.imglib2.cache.img.DirtyDiskCellCache;
import net.imglib2.cache.img.DiskCellCache;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.array.DirtyShortArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.RealComposite;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class ClassifierTraining
{
	public static class CheckerboardLoader implements CacheLoader< Long, Cell< DirtyShortArray > >
	{
		private final CellGrid grid;

		public CheckerboardLoader( final CellGrid grid )
		{
			this.grid = grid;
		}

		@Override
		public Cell< DirtyShortArray > get( final Long key ) throws Exception
		{
			final long index = key;

			final int n = grid.numDimensions();
			final long[] cellMin = new long[ n ];
			final int[] cellDims = new int[ n ];
			grid.getCellDimensions( index, cellMin, cellDims );
			final int blocksize = ( int ) Intervals.numElements( cellDims );
			final DirtyShortArray array = new DirtyShortArray( blocksize );

			final long[] cellGridPosition = new long[ n ];
			grid.getCellGridPositionFlat( index, cellGridPosition );
			long sum = 0;
			for ( int d = 0; d < n; ++d )
				sum += cellGridPosition[ d ];
			final short color = ( short ) ( ( sum & 0x01 ) == 0 ? 0x0000 : 0xffff );
			Arrays.fill( array.getCurrentStorageArray(), color );

			return new Cell<>( cellDims, cellMin, array );
		}
	}


	static < T extends RealType< T > > Pair< Img< UnsignedShortType >, Img< VolatileUnsignedShortType > >
	createClassifier( final RandomAccessibleInterval< T > source, final Classifier classifier, final int numClasses, final CellGrid grid, final BlockingFetchQueues< Callable< ? > > queue )
			throws IOException
	{
		final UnsignedShortType type = new UnsignedShortType();
		final VolatileUnsignedShortType vtype = new VolatileUnsignedShortType();

		final Path blockcache = DiskCellCache.createTempDirectory( "Classifier", true );
		final DiskCellCache< VolatileShortArray > diskcache = new DiskCellCache<>(
				blockcache,
				grid,
				new ClassifyingCellLoader<>( grid, Views.collapseReal( source ), classifier, ( int ) source.dimension( source.numDimensions() - 1 ), numClasses ),
				AccessIo.get( SHORT, VOLATILE ),
				type.getEntitiesPerPixel() );
		final IoSync< Long, Cell< VolatileShortArray > > iosync = new IoSync<>( diskcache );
		final Cache< Long, Cell< VolatileShortArray > > cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< VolatileShortArray > >( 1000 )
				.withRemover( iosync )
				.withLoader( iosync );
		final Img< UnsignedShortType > prediction = new LazyCellImg<>( grid, type, cache.unchecked()::get );

		final CreateInvalid< Long, Cell< VolatileShortArray > > createInvalid = CreateInvalidVolatileCell.get( grid, type );
		final VolatileCache< Long, Cell< VolatileShortArray > > volatileCache = new WeakRefVolatileCache<>( cache, queue, createInvalid );

		final CacheHints hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		final VolatileCachedCellImg< VolatileUnsignedShortType, ? > vprediction = new VolatileCachedCellImg<>( grid, vtype, hints, volatileCache.unchecked()::get );

		return new ValuePair<>( prediction, vprediction );
	}

	public static < T extends RealType< T > > Pair< Img< FloatType >, Img< VolatileFloatType > >
	createFeature( final FeatureGenerator< T, FloatType > generator, final CellGrid grid, final BlockingFetchQueues< Callable< ? > > queue, final String cacheName )
			throws IOException
	{
		final FloatType type = new FloatType();
		final VolatileFloatType vtype = new VolatileFloatType();

		final Path blockcache = DiskCellCache.createTempDirectory( cacheName, true );
		final DiskCellCache< VolatileFloatArray > diskcache = new DiskCellCache<>(
				blockcache,
				grid,
				new FeatureGeneratorLoader<>( grid, generator ),
				AccessIo.get( FLOAT, VOLATILE ),
				type.getEntitiesPerPixel() );
		final IoSync< Long, Cell< VolatileFloatArray > > iosync = new IoSync<>( diskcache );
		final Cache< Long, Cell< VolatileFloatArray > > cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< VolatileFloatArray > >( 1000 )
				.withRemover( iosync )
				.withLoader( iosync );
		final Img< FloatType > features = new LazyCellImg<>( grid, type, cache.unchecked()::get );

		final CreateInvalid< Long, Cell< VolatileFloatArray > > createInvalid = CreateInvalidVolatileCell.get( grid, type );
		final VolatileCache< Long, Cell< VolatileFloatArray > > volatileCache = new WeakRefVolatileCache<>( cache, queue, createInvalid );

		final CacheHints hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		final VolatileCachedCellImg< VolatileFloatType, ? > vfeatures = new VolatileCachedCellImg<>( grid, vtype, hints, volatileCache.unchecked()::get );

		return new ValuePair<>( features, vfeatures );

	}

	public static void main( final String[] args ) throws Exception
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final int[] cellDimensions = new int[] { 64, 64, 64 };
		final long[] dimensions = new long[] { 640, 640, 640 };

		final UnsignedShortType type = new UnsignedShortType();

		final CellGrid grid = new CellGrid( dimensions, cellDimensions );
		final Path blockcache = DiskCellCache.createTempDirectory( "CellImg-", true );
		final DiskCellCache< DirtyShortArray > diskcache = new DirtyDiskCellCache<>(
				blockcache,
				grid,
				new CheckerboardLoader( grid ),
				AccessIo.get( SHORT, DIRTY ),
				type.getEntitiesPerPixel() );
		final IoSync< Long, Cell< DirtyShortArray > > iosync = new IoSync<>( diskcache );
		final UncheckedCache< Long, Cell< DirtyShortArray > > cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< DirtyShortArray > >( 1000 )
				.withRemover( iosync )
				.withLoader( iosync )
				.unchecked();
		final Img< UnsignedShortType > img = new LazyCellImg<>( grid, new UnsignedShortType(), cache::get );

		final BdvOptions options = dimensions.length == 2 ? BdvOptions.options().is2D() : BdvOptions.options();
		final Bdv bdv = BdvFunctions.show( img, "Cached", options );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( SINGLE );


		final int maxNumLevels = 1;
		final int numFetcherThreads = 7;
		final BlockingFetchQueues< Callable< ? > > queue = new BlockingFetchQueues<>( maxNumLevels );
		new FetcherThreads( queue, numFetcherThreads );


		final double[][] sigmas = {
				{ 1.0, 1.0, 1.0 },
				{ 2.0, 2.0, 2.0 },
				{ 5.0, 5.0, 5.0 },
				{ 1.0, 9.0, 1.0 },
				{ 16.0, 16.0, 16.0 },
		};

		final Pair< Img< FloatType >, Img< VolatileFloatType > >[] gaussians = new Pair[ sigmas.length ];
		final Pair< Img< FloatType >, Img< VolatileFloatType > >[][] gradients = new Pair[ sigmas.length ][];
//		final Pair< Img< FloatType >, Img< VolatileFloatType > >[] gradientMagnitudes = new Pair[ sigmas.length ];
		for ( int sigmaIndex = 0; sigmaIndex < sigmas.length; ++sigmaIndex )
		{
			final int sI = sigmaIndex;
			final FunctionFeatureGenerator< FloatType, FloatType > gaussGen = new FunctionFeatureGenerator<>( ( s, t ) -> {
				try
				{
					Gauss3.gauss( sigmas[ sI ], s, t, Executors.newSingleThreadExecutor() );
				}
				catch ( final IncompatibleTypeException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			},
					Converters.convert( Views.extendBorder( img ), new RealFloatConverter<>(), new FloatType() ) );
			final long[] extendedDims = LongStream.concat( Arrays.stream( dimensions ), LongStream.of( gaussGen.numFeatures() ) ).toArray();
			final int[] extendedCellDims = IntStream.concat( Arrays.stream( cellDimensions ), IntStream.of( gaussGen.numFeatures() ) ).toArray();
			final CellGrid gaussGrid = new CellGrid( extendedDims.clone(), extendedCellDims.clone() );
			gaussians[ sI ] = createFeature( gaussGen, gaussGrid, queue, "gauss-" + Arrays.toString( sigmas[ sI ] ) );

			final Pair< Img< FloatType >, Img< VolatileFloatType > >[] gradientsForSigma = new Pair[ dimensions.length ];
			for ( int d = 0; d < gradientsForSigma.length; ++d )
			{
				final int fd = d;
				final FunctionFeatureGenerator< FloatType, FloatType > gradientGen = new FunctionFeatureGenerator<>( ( s, t ) -> {
					PartialDerivative.gradientCentralDifference2( s, t, fd );
					return null;
				},
						Views.extendBorder( Views.hyperSlice( gaussians[ sI ].getA(), dimensions.length, 0l ) ) );
				extendedDims[ extendedDims.length - 1 ] = gradientGen.numFeatures();
				extendedCellDims[ extendedCellDims.length - 1 ] = gradientGen.numFeatures();
				final CellGrid gradientGrid = new CellGrid( extendedDims.clone(), extendedCellDims.clone() );
				gradientsForSigma[ d ] = createFeature( gradientGen, gradientGrid, queue, "gradient-" + d + "-" + Arrays.toString( sigmas[ sI ] ) );
			}
			gradients[ sI ] = gradientsForSigma;
		}

		final ArrayList< RandomAccessibleInterval< FloatType > > featuresList = new ArrayList<>();
		final ArrayList< RandomAccessibleInterval< VolatileFloatType > > vfeaturesList = new ArrayList<>();
		for ( int sigmaIndex = 0; sigmaIndex < sigmas.length; ++sigmaIndex )
		{
			{
				final Pair< Img< FloatType >, Img< VolatileFloatType > > gaussian = gaussians[ sigmaIndex ];
				final int lastDim = gaussian.getA().numDimensions() - 1;
				final long min = gaussian.getA().min( lastDim );
				final long max = gaussian.getA().max( lastDim );
				for ( long idx = min; idx <= max; ++idx ) {
					featuresList.add( Views.hyperSlice( gaussian.getA(), lastDim, idx ) );
					vfeaturesList.add( Views.hyperSlice( gaussian.getB(), lastDim, idx ) );
				}
			}
			{
				final Pair< Img< FloatType >, Img< VolatileFloatType > >[] grads = gradients[ sigmaIndex ];
				for ( int d = 0; d < grads.length; ++d ) {
					final Pair< Img< FloatType >, Img< VolatileFloatType > > grad = grads[d];
					final int lastDim = grad.getA().numDimensions() - 1;
					final long min = grad.getA().min( lastDim );
					final long max = grad.getA().max( lastDim );
					for ( long idx = min; idx <= max; ++idx ) {
						featuresList.add( Views.hyperSlice( grad.getA(), lastDim, idx ) );
						vfeaturesList.add( Views.hyperSlice( grad.getB(), lastDim, idx ) );
					}
				}
			}
		}

		final ValuePair< RandomAccessibleInterval< FloatType >, RandomAccessibleInterval< VolatileFloatType > > features = new ValuePair<>( Views.stack( featuresList ), Views.stack( vfeaturesList ) );
		final int numFeatures = ( int ) features.getA().dimension( features.getA().numDimensions() - 1 );

		BdvFunctions.show( features.getB(), "features", options );

		final long[] trainingDimensions = Arrays.stream( cellDimensions ).mapToLong( i -> 2 * i ).toArray();
		trainingDimensions[ 2 ] = 1;
		final FinalInterval trainingInterval = new FinalInterval( trainingDimensions );
		final ArrayList< Attribute > attributes = new ArrayList<>();
		for ( int i = 0; i < numFeatures; ++i )
			attributes.add( new Attribute( "" + i ) );
		final ArrayList< String > classes = new ArrayList<>();
		classes.add( "0" );
		classes.add( "1" );
		attributes.add( new Attribute( "class", classes ) );
		final Instances instances = new Instances( "training", attributes, ( int ) Intervals.numElements( trainingDimensions ) );
		instances.setClassIndex( numFeatures );
		for ( final Pair< UnsignedShortType, RealComposite< VolatileFloatType > > pair : Views.interval( Views.pair( img, Views.collapseReal( features.getB() ) ), trainingInterval ) )
		{
			final int label = pair.getA().get() > 0 ? 1 : 0;
			final RealComposite< VolatileFloatType > feat = pair.getB();
			final double[] values = new double[ numFeatures + 1 ];
			for ( int f = 0; f < numFeatures; ++f )
				values[ f ] = feat.get( f ).getRealDouble();
			values[ numFeatures ] = label;
			instances.add( new DenseInstance( 1.0, values ) );
		}

		final RandomForest classifier = new RandomForest();
		classifier.buildClassifier( instances );

		final Pair< Img< UnsignedShortType >, Img< VolatileUnsignedShortType > > classified = createClassifier( features.getA(), classifier, 2, grid, queue );
		final BdvStackSource< VolatileUnsignedShortType > classifiedBdv = BdvFunctions.show( classified.getB(), "classified", options );
		classifiedBdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 1 );

	}
}
