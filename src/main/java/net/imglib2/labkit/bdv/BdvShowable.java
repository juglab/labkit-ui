package net.imglib2.labkit.bdv;

import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.type.numeric.NumericType;
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
		return new FinalInterval( setup.getSize() );
	}

	public VoxelDimensions voxelSize()
	{
		if(image != null)
			return defaultVoxelSize();
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		BasicViewSetup setup = getFirst( seq );
		if(setup.hasVoxelSize())
			return setup.getVoxelSize();
		return defaultVoxelSize();
	}

	private FinalVoxelDimensions defaultVoxelSize()
	{
		return new FinalVoxelDimensions( null, IntStream.range( 0, interval().numDimensions() ).mapToDouble( x -> 1.0 ).toArray() );
	}

	private BasicViewSetup getFirst( AbstractSequenceDescription< ?, ?, ? > seq )
	{
		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() ) {
			return setup;
		}
		throw new IllegalStateException( "SpimData contains no setup." );
	}
}
