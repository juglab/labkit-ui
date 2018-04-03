package net.imglib2.labkit.bdv;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

class SpimBdvShowable implements BdvShowable {

	private final AbstractSpimData< ? > spimData;

	SpimBdvShowable(AbstractSpimData<?> spimData)
	{
		this.spimData = spimData;
	}

	@Override
	public Interval interval()
	{
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

	@Override
	public AffineTransform3D transformation()
	{
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		BasicViewSetup setup = getFirst( seq );
		TimePoint firstTime = seq.getTimePoints().getTimePointsOrdered().get( 0 );
		ViewRegistration registration = spimData.getViewRegistrations().getViewRegistration( firstTime.getId(), setup.getId() );
		return registration.getModel();
	}

	@Override
	public BdvSource show(String title, BdvOptions options) {
		return BdvFunctions.show(spimData, options ).get(0);
	}

	private BasicViewSetup getFirst( AbstractSequenceDescription< ?, ?, ? > seq )
	{
		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() ) {
			return setup;
		}
		throw new IllegalStateException( "SpimData contains no setup." );
	}
}
