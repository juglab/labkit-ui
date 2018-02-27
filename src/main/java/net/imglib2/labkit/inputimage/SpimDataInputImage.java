package net.imglib2.labkit.inputimage;

import bdv.ViewerSetupImgLoader;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.type.numeric.NumericType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpimDataInputImage implements InputImage
{

	private final AbstractSpimData< ? > spimData;

	public SpimDataInputImage( AbstractSpimData<?> spimData ) {
		this.spimData = spimData;
	}

	@Override public BdvShowable showable()
	{
		return BdvShowable.wrap( spimData );
	}

	@Override public RandomAccessibleInterval< ? extends NumericType< ? > > imageForSegmentation()
	{
		BasicViewSetup setup = getSetup();
		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
		ViewerSetupImgLoader< ?, ? > imgLoader = ( ViewerSetupImgLoader ) seq.getImgLoader().getSetupImgLoader( setup.getId() );
		RandomAccessibleInterval< ? > image = imgLoader.getImage( 0, 2 );
		return LabkitUtils.uncheckedCast( image );
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
		return spimData.getBasePath().getAbsolutePath();
	}

	@Override public String getName()
	{
		return spimData.getBasePath().getName();
	}

	@Override public List< CalibratedAxis > axes()
	{
		BasicViewSetup setup = getSetup();
		VoxelDimensions voxelSize = setup.getVoxelSize();
		return IntStream.range(0, voxelSize.numDimensions())
				.mapToObj( index -> new DefaultLinearAxis( voxelSize.dimension( index ) ) )
				.collect( Collectors.toList());
	}

	private BasicViewSetup getSetup()
	{
		return spimData.getSequenceDescription().getViewSetupsOrdered().get(0);
	}

	@Override public boolean isTimeSeries()
	{
		return false;
	}
}
