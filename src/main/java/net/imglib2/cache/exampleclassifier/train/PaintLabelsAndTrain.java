package net.imglib2.cache.exampleclassifier.train;

import java.util.Random;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileRealType;

public class PaintLabelsAndTrain
{

	public static void main( final String[] args )
	{

		final float[] data = new float[ 300 * 400 * 30 ];
		final ArrayImg< FloatType, FloatArray > rawData = ArrayImgs.floats( data, 300, 400, 30 );
		final Random rng = new Random();
		for ( int d = 0; d < data.length; ++d )
			data[ d ] = rng.nextFloat() * (1 << 16 );
		System.out.println( data[ 0 ] + " " + data[ 1 ] + " " + data[ 2 ] + " " + data[ 3 ] );
		final ArrayImg< IntType, IntArray > labels = ArrayImgs.ints( 300, 400, 30 );
		trainClassifier( rawData, null, null, labels, rng );
	}

	public static < R extends RealType< R >, F extends RealType< F >, VF extends VolatileRealType< F >, L extends IntegerType< L > >
	void trainClassifier(
			final RandomAccessibleInterval< R > rawData,
			final RandomAccessibleInterval< F > features,
			final RandomAccessibleInterval< VF > volatileFeatures,
			final RandomAccessibleInterval< L > labels,
			final Random rng )
	{

		final int nLabels = 3;
		final TIntIntHashMap cmap = new TIntIntHashMap();
		cmap.put( 0, 0 );
		for ( int i = 0; i < nLabels; ++i )
			cmap.put( i + 1, rng.nextInt() );

		final Converter< L, ARGBType > conv = ( input, output ) -> {
			output.set( cmap.get( input.getInteger() ) );
		};

		final BdvStackSource< ? extends RealType< ? > > bdv = BdvFunctions.show( rawData, "raw" );
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.SINGLE );
		BdvFunctions.show( Converters.convert( labels, conv, new ARGBType() ), "labels", BdvOptions.options().addTo( bdv ) );
		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();

		final InputTriggerConfig config = new InputTriggerConfig();
		final Behaviours behaviors = new Behaviours( config );
		behaviors.install( bdv.getBdvHandle().getTriggerbindings(), "paint ground truth" );
		final LabelBrushController< ? extends IntegerType< ? > > brushController = new LabelBrushController<>(
				viewer,
				labels,
				new AffineTransform3D(),
				behaviors,
				nLabels );
		bdv.getBdvHandle().getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
		brushController.getBrushOverlay().setCmap( cmap );



	}

}
