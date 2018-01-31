package net.imglib2.labkit.classification;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.BdvLayer;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.RandomAccessibleContainer;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class PredictionLayer implements BdvLayer
{

	private final Extensible extensible;

	private final SegmentationResultsModel model;

	private final RandomAccessibleContainer< VolatileARGBType > segmentationContainer;

	private Notifier< Runnable > listeners = new Notifier<>();

	private RandomAccessibleInterval< ? extends NumericType< ? > > view;

	private AffineTransform3D transformation;

	public PredictionLayer( Extensible extensible, SegmentationResultsModel model )
	{
		this.model = model;
		this.extensible = extensible;
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), model.interval().numDimensions() );
		this.segmentationContainer = new RandomAccessibleContainer<>( emptyPrediction );
		this.transformation = scaleTransformation( model.scaling() );
		this.view = Views.interval( segmentationContainer, model.interval() );
		model.segmentationChangedListeners().add( this::classifierChanged );
	}

	private static AffineTransform3D scaleTransformation( double scaling )
	{
		AffineTransform3D transformation = new AffineTransform3D();
		transformation.scale( scaling );
		return transformation;
	}

	private void classifierChanged()
	{
		final RandomAccessibleInterval<VolatileARGBType> converted = coloredVolatileView();
		segmentationContainer.setSource(Views.extendValue( converted, new VolatileARGBType(0)  ));
		listeners.forEach( Runnable::run );
	}

	private RandomAccessibleInterval<VolatileARGBType > coloredVolatileView() {
		ARGBType[] colors = model.colors().toArray(new ARGBType[0]);
		return mapColors(colors, extensible.wrapAsVolatile(model.segmentation()));
	}

	private RandomAccessibleInterval<VolatileARGBType> mapColors(ARGBType[] colors, RandomAccessibleInterval<VolatileShortType > source) {
		final Converter< VolatileShortType, VolatileARGBType > conv = ( input, output ) -> {
			final boolean isValid = input.isValid();
			output.setValid( isValid );
			if ( isValid )
				output.set(colors[input.get().get()].get());
		};

		return Converters.convert(source, conv, new VolatileARGBType() );
	}

	@Override public RandomAccessibleInterval< ? extends NumericType< ? > > image()
	{
		return view;
	}

	@Override public Notifier< Runnable > listeners()
	{
		return listeners;
	}

	@Override public String title()
	{
		return "Segmentation";
	}

	@Override public AffineTransform3D transformation()
	{
		return transformation;
	}
}
