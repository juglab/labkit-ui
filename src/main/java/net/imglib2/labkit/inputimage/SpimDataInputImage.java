package net.imglib2.labkit.inputimage;

import bdv.ViewerSetupImgLoader;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO: Lot's of code in this class is hardcoded for a specific HDF5 image, it need's to be generalized to other hdf5 files.
public class SpimDataInputImage implements InputImage
{

	private final AbstractSpimData< ? > spimData;

	private final RandomAccessibleInterval< ? > imageForSegmentation;

	private final String filename;

	private final boolean timeseries;

	public SpimDataInputImage( String filename ) {
		this.spimData = RevampUtils.wrapException( () -> new XmlIoSpimDataMinimal().load( filename ) );
		this.filename = filename;
		this.timeseries = spimData.getSequenceDescription().getTimePoints().size() > 1;
		this.imageForSegmentation = initImageForSegmentation();
	}

	@Override public BdvShowable showable()
	{
		return BdvShowable.wrap( spimData );
	}

	@Override public RandomAccessibleInterval< ? extends NumericType< ? > > imageForSegmentation()
	{
		RandomAccessibleInterval< ? > image = initImageForSegmentation();
		return LabkitUtils.uncheckedCast( image );
	}

	private RandomAccessibleInterval< ? > initImageForSegmentation()
	{
		BasicViewSetup setup = getSetup();
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		ViewerSetupImgLoader< ?, ? > imgLoader = ( ViewerSetupImgLoader ) seq.getImgLoader().getSetupImgLoader( setup.getId() );
		List< TimePoint > timePoints = seq.getTimePoints().getTimePointsOrdered();
		return combineFrames( imgLoader, timePoints );
	}

	private <T> RandomAccessibleInterval< ? > combineFrames( ViewerSetupImgLoader< T, ? > imgLoader, List< TimePoint > timePoints )
	{
		if(timePoints.size() == 1)
			return imgLoader.getImage( timePoints.get(0).getId(), 2 );
		List< RandomAccessibleInterval< T > > slices = timePoints.stream().map( t -> imgLoader.getImage( t.getId(), 2 ) ).collect( Collectors.toList() );
		return Views.stack(slices);
	}

	@Override public ChannelSetting getChannelSetting()
	{
		return ChannelSetting.SINGLE;
	}

	@Override public int getSpatialDimensions()
	{
		return 3;
	}

	@Override public String getFilename()
	{
		return filename;
	}

	@Override public String getName()
	{
		return new File(filename).getName();
	}

	@Override public List< CalibratedAxis > axes()
	{
		VoxelDimensions voxelSize = getVoxelDimensions();
		return IntStream.range(0, voxelSize.numDimensions())
				.mapToObj( index -> new DefaultLinearAxis( voxelSize.dimension( index ) ) )
				.collect( Collectors.toList());
	}

	private VoxelDimensions getVoxelDimensions()
	{
		BasicViewSetup setup = getSetup();
		if(setup.hasVoxelSize())
			return setup.getVoxelSize();
		return defaultVoxelSize();
	}

	private FinalVoxelDimensions defaultVoxelSize()
	{
		return new FinalVoxelDimensions( null, IntStream.range( 0, interval().numDimensions() ).mapToDouble( x -> 1.0 ).toArray() );
	}


	private BasicViewSetup getSetup()
	{
		return spimData.getSequenceDescription().getViewSetupsOrdered().get(0);
	}

	@Override public boolean isTimeSeries()
	{
		return timeseries;
	}
}
