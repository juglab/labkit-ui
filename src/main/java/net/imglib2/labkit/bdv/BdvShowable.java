package net.imglib2.labkit.bdv;

import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;

import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class BdvShowable
{

	private final RandomAccessibleInterval< ? extends NumericType<?> > image;
	private final AbstractSpimData< ? > spimData;

	private BdvShowable( RandomAccessibleInterval< ? extends NumericType< ? > > image, AbstractSpimData<?> spimData )
	{
		this.image = image;
		this.spimData = spimData;
	}

	public static BdvShowable wrap( RandomAccessibleInterval< ? extends NumericType< ? > > image )
	{
		return new BdvShowable( Objects.requireNonNull( image ), null );
	}

	public static BdvShowable wrap( AbstractSpimData<?> spimData )
	{
		return new BdvShowable( null, Objects.requireNonNull( spimData ) );
	}

	public boolean isSpimData() {
		return spimData != null;
	}

	public RandomAccessibleInterval< ? extends NumericType< ? > > image() {
		if(image == null)
			throw new IllegalStateException( "" );
		return image;
	}

	public AbstractSpimData<?> spimData() {
		if(spimData == null)
			throw new IllegalStateException( "" );
		return spimData;
	}

	public Pair< Double, Double > minMax() {
		if(image == null)
			throw new IllegalStateException( "" );
		return LabkitUtils.estimateMinMax(image());
	}

	public Interval interval()
	{
		if(image != null)
			return new FinalInterval(image);
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		BasicViewSetup setup = getFirst( seq );
		Dimensions size = setup.getSize();
		if(size == null)
		{
			RandomAccessibleInterval< ? > image = seq.getImgLoader().getSetupImgLoader( setup.getId() ).getImage( 0 );
			return new FinalInterval( image );
		}
		return new FinalInterval( size );
	}

	public AffineTransform3D transformation()
	{
		if(image != null)
			return new AffineTransform3D();
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		BasicViewSetup setup = getFirst( seq );
		TimePoint firstTime = seq.getTimePoints().getTimePointsOrdered().get( 0 );
		ViewRegistration registration = spimData.getViewRegistrations().getViewRegistration( firstTime.getId(), setup.getId() );
		return registration.getModel();
	}

	private BasicViewSetup getFirst( AbstractSequenceDescription< ?, ?, ? > seq )
	{
		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() ) {
			return setup;
		}
		throw new IllegalStateException( "SpimData contains no setup." );
	}
}
