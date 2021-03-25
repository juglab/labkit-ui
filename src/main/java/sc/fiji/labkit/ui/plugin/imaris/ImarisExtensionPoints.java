package sc.fiji.labkit.ui.plugin.imaris;

import Imaris.Error;
import Imaris.IApplicationPrx;
import Imaris.IDataSetPrx;
import Imaris.tType;
import bdv.util.AxisOrder;
import com.bitplane.xt.ImarisApplication;
import com.bitplane.xt.ImarisDataset;
import com.bitplane.xt.img.ImarisCachedCellImgOptions;
import com.bitplane.xt.img.ImarisCachedLabelImgFactory;
import com.bitplane.xt.img.ImarisCachedProbabilitiesImg;
import com.bitplane.xt.img.ImarisCachedProbabilitiesImgFactory;
import com.bitplane.xt.img.ImarisImg;
import com.bitplane.xt.util.AxisOrderUtils;
import com.bitplane.xt.util.ImarisUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.segmentation.Segmenter;

import static Imaris.tType.eTypeUInt16;
import static Imaris.tType.eTypeUInt8;

public class ImarisExtensionPoints
{
	private final DefaultSegmentationModel model;

	private final ImarisApplication imaris;

	private final String storeClassifiersPath;

	public ImarisExtensionPoints( final DefaultSegmentationModel model, final ImarisApplication imaris, final String storeClassifiersPath )
	{
		this.model = model;
		this.imaris = imaris;
		this.storeClassifiersPath = storeClassifiersPath;
	}

	public void registerImageFactories()
	{
		model.extensionPoints().setCachedPredictionImageFactory( this::setupCachedImagePrediction );
		model.extensionPoints().setCachedSegmentationImageFactory( this::setupCachedImageSegmentation );
	}

	/**
	 * Called when the prediction image (class probabilities) is completely computed.
	 * Adds the corresponding dataset to the displayed images in Imaris.
	 */
	public < T extends NumericType< T > & NativeType< T > > void setImarisImage( final RandomAccessibleInterval< T > result )
	{
		if ( result instanceof ImarisImg )
		{
			final ImarisImg imarisCached = ( ImarisImg ) result;
			try
			{
				imarisCached.persist();
				final IDataSetPrx dataset = imarisCached.getIDataSetPrx();
				final IApplicationPrx app = imaris.getIApplicationPrx();
				final int numImages = app.GetNumberOfImages();
				app.SetImage( numImages, dataset );
				// TODO: close labkit window if called with "special" ImarisInstance?
			}
			catch ( Error error )
			{
				error.printStackTrace();
				throw new RuntimeException( error );
			}
		}
	}

	public void reportProgress( final double completionRatio )
	{
		try
		{
			final String s = String.format( "<Labkit><Progress>%.2f</Progress></Labkit>", completionRatio );
			System.out.println( "imaris.getIApplicationPrx().Execute(): " + s );
			imaris.getIApplicationPrx().Execute( s );
		}
		catch ( Error error )
		{
			error.printStackTrace();
		}
	}

	private < T extends NativeType< T > > Img< T > setupCachedImagePrediction(
			final Segmenter segmenter,
			final Consumer<RandomAccessibleInterval<T>> loader,
			final CellGrid grid,
			final T type )
	{
		String classifierFilename = null;
		if ( storeClassifiersPath != null )
		{
			try
			{
				final Path dir = Paths.get( storeClassifiersPath );
				if ( !Files.isDirectory( dir ) )
					throw new IOException( "Cannot store classifier. \"" + storeClassifiersPath + "\" is not a valid directory" );

				final Path path = Files.createTempFile( dir, "labkit", ".classifier" );
				classifierFilename = path.toString();
				segmenter.saveModel( classifierFilename );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}

		if ( !isImarisInput() )
			throw new IllegalArgumentException();
		if ( !segmenter.isTrained() )
			throw new IllegalArgumentException();

		final List< String > labels = segmenter.classNames();
		final List< ARGBType > colors = labels.stream().map( this::getLabelColor ).collect( Collectors.toList() );

		final int[] cellDimensions = getCellDimensions( grid );
		final long[] imgDimensions = grid.getImgDimensions();
		Arrays.setAll( cellDimensions, i -> ( int ) Math.min( cellDimensions[ i ], imgDimensions[ i ] ) );
		ImarisCachedCellImgOptions options = ImarisCachedCellImgOptions.options()
				// .cacheType( CacheType.BOUNDED )
				// .maxCacheSize( 1000 )
				.dirtyAccesses( true )
				.initializeCellsAsDirty( false )
				.persistOnLoad( true )
				.cellDimensions( cellDimensions );
		final ImarisCachedProbabilitiesImgFactory< T > factory = new ImarisCachedProbabilitiesImgFactory<>( type, imaris, options );

		ImgPlus< ? > image = model.imageLabelingModel().imageForSegmentation().get();
		if ( ImgPlusViewsOld.hasAxis( image, Axes.CHANNEL ) )
			image = ImgPlusViewsOld.hyperSlice( image, Axes.CHANNEL, 0 );

		try
		{
			// NB: createDataset() will add labels-1 channel dimension
			// so we need to strip the last element from imgDimensions
			final IDataSetPrx dataset = createDataset(
					imaris.getIApplicationPrx(),
					eTypeUInt8,
					labels.size() - 1,
					AxisOrderUtils.getAxisOrder( image ),
					Arrays.copyOf( imgDimensions, imgDimensions.length - 1 ) );
			copyCalibration( getImarisDataset().getIDataSetPrx(), dataset );

			final int nChannels = labels.size() - 1;
			for ( int c = 0; c < nChannels; c++ )
			{
				final int argb = colors.get( c + 1 ).get();
				final int color = ( ( ARGBType.blue( argb ) & 0xff ) << 16 ) |
						( ( ARGBType.green( argb ) & 0xff ) << 8 ) |
						( ARGBType.red( argb ) & 0xff );
				dataset.SetChannelName( c, labels.get( c + 1 ) );
				dataset.SetChannelColorRGBA( c, color );
				dataset.SetChannelRange( c, 0, 255 );
			}

			if ( classifierFilename != null )
			{
				dataset.SetParameter( "Labkit", "ClassifierFile", classifierFilename );
			}

//			factory.getImarisService().app().SetImage( 0, dataset );
			final ImarisCachedProbabilitiesImg< T, ? > img = factory.create( dataset, imgDimensions, loader::accept );
			return img;
		}
		catch ( Error error )
		{
			throw new RuntimeException( error );
		}
	}

	private < T extends NativeType< T > > Img< T > setupCachedImageSegmentation(
			final Segmenter segmenter,
			final Consumer<RandomAccessibleInterval<T>> loader,
			final CellGrid grid,
			final T type )
	{
		if ( !isImarisInput() )
			throw new IllegalArgumentException();
		if ( !segmenter.isTrained() )
			throw new IllegalArgumentException();

		final List< String > labels = segmenter.classNames();
		final List< ARGBType > colors = labels.stream().map( this::getLabelColor ).collect( Collectors.toList() );

		final int[] cellDimensions = getCellDimensions( grid );
		final long[] imgDimensions = grid.getImgDimensions();
		Arrays.setAll( cellDimensions, i -> ( int ) Math.min( cellDimensions[ i ], imgDimensions[ i ] ) );

		// TODO: use imaris default cell size? (does this improve performance?	)
		// examples sizes: [256 * 256 * 8]

		ImarisCachedCellImgOptions options = ImarisCachedCellImgOptions.options()
				// .cacheType( CacheType.BOUNDED )
				// .maxCacheSize( 1000 )
				.dirtyAccesses( true )
				.initializeCellsAsDirty( false )
				.persistOnLoad( true )
				.cellDimensions( cellDimensions );
		final ImarisCachedLabelImgFactory< T > factory = new ImarisCachedLabelImgFactory<>( type, imaris, options );

		// We need calibration metadata to setup the imaris image correctly.
		// We simply strip channel dimension from imageForSegmentation.
		ImgPlus< ? > image = model.imageLabelingModel().imageForSegmentation().get();
		if ( ImgPlusViewsOld.hasAxis( image, Axes.CHANNEL ) )
			image = ImgPlusViewsOld.hyperSlice( image, Axes.CHANNEL, 0 );

		try
		{
			final IDataSetPrx dataset = createDataset(
					imaris.getIApplicationPrx(),
					eTypeUInt16,
					labels.size() - 1,
					AxisOrderUtils.getAxisOrder( image ),
					imgDimensions );
			copyCalibration( getImarisDataset().getIDataSetPrx(), dataset );

			final int nChannels = labels.size() - 1;
			for ( int c = 0; c < nChannels; c++ )
			{
				final int argb = colors.get( c + 1 ).get();
				final int color = ( ( ARGBType.blue( argb ) & 0xff ) << 16 ) |
						( ( ARGBType.green( argb ) & 0xff ) << 8 ) |
						( ARGBType.red( argb ) & 0xff );
				dataset.SetChannelName( c, labels.get( c + 1 ) );
				dataset.SetChannelColorRGBA( c, color );
				dataset.SetChannelRange( c, 0, 1 );
			}

//			factory.getImarisService().app().SetImage( 0, dataset );
			return factory.create( dataset, imgDimensions, loader::accept );
		}
		catch ( Error error )
		{
			throw new RuntimeException( error );
		}
	}

	private ARGBType getLabelColor( String name )
	{
		Labeling labeling = model.imageLabelingModel().labeling().get();
		try
		{
			return labeling.getLabel( name ).color();
		}
		catch ( NoSuchElementException e )
		{
			return labeling.addLabel( name ).color();
		}
	}

	private static int[] getCellDimensions( CellGrid grid )
	{
		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		return cellDimensions;
	}

	private boolean isImarisInput()
	{
		return model.imageLabelingModel().showable().get() instanceof ImarisInputImage.ImarisShowable;
	}

	/**
	 * Used for setting up segmentation result.
	 * Adds a channel dimension with extend labels.size() - 1.
	 */
	private IDataSetPrx createDataset(
			final IApplicationPrx app,
			final tType type,
			final int nChannels,
			final AxisOrder axisOrder,
			final long... dimensions ) throws Error
	{
		final int sx = ( int ) dimensions[ 0 ];
		final int sy = ( int ) dimensions[ 1 ];
		final int sz;
		final int sc = nChannels;
		final int st;
		switch ( axisOrder )
		{
		case XY:
			sz = 1;
			st = 1;
			break;
		case XYZ:
			sz = ( int ) dimensions[ 2 ];
			st = 1;
			break;
		case XYT:
			sz = 1;
			st = ( int ) dimensions[ 2 ];
			break;
		case XYZT:
			sz = ( int ) dimensions[ 2 ];
			st = ( int ) dimensions[ 3 ];
			break;
		default:
			throw new IllegalArgumentException();
		}
		return ImarisUtils.createDataset( app, type, sx, sy, sz, sc, st );
	}

	/**
	 * Extract ImarisDataset from {@link ImageLabelingModel#showable()} (which
	 * is expected to be a {@code ImarisShowable}).
	 */
	private ImarisDataset< ? > getImarisDataset()
	{
		final BdvShowable s = model.imageLabelingModel().showable().get();
		if ( s instanceof ImarisInputImage.ImarisShowable )
			return ( ( ImarisInputImage.ImarisShowable ) s ).getDataset();
		else
			throw new IllegalStateException();
	}

	/**
	 * Copy calibration settings from one {@code IDataSetPrx} to another.
	 */
	private void copyCalibration( IDataSetPrx from, IDataSetPrx to ) throws Error
	{
		to.SetExtendMinX( from.GetExtendMinX() );
		to.SetExtendMinY( from.GetExtendMinY() );
		to.SetExtendMinZ( from.GetExtendMinZ() );
		to.SetExtendMaxX( from.GetExtendMaxX() );
		to.SetExtendMaxY( from.GetExtendMaxY() );
		to.SetExtendMaxZ( from.GetExtendMaxZ() );
		to.SetTimePointsDelta( from.GetTimePointsDelta() );
		final int sizeT = from.GetSizeT();
		if ( to.GetSizeT() != sizeT )
			throw new IllegalArgumentException();
		for ( int i = 0; i < sizeT; i++ )
			to.SetTimePoint( i, from.GetTimePoint( i ) );
	}
}
