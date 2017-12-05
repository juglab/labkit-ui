package net.imglib2.atlas.control.brush.neighborhood;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

public class NeighborhoodPixelsGeneratorForTimeSeries< T, S extends Iterator< T > & Localizable >
implements PaintPixelsGenerator< T, IteratorOverBackMappingPoints< T, S > >
{

	private final int timeAxis;

	private final PaintPixelsGenerator< T, S > spatialPixelGenerator;

	public NeighborhoodPixelsGeneratorForTimeSeries( final int timeAxis, final PaintPixelsGenerator< T, S > spatialPixelGenerator )
	{
		super();
		this.timeAxis = timeAxis;
		this.spatialPixelGenerator = spatialPixelGenerator;
	}

	@Override
	public IteratorOverBackMappingPoints< T, S > getPaintPixels( final RandomAccessible< T > accessible, final RealLocalizable position, final int timestep, final int size )
	{
		final MixedTransformView< T > hs = Views.hyperSlice( accessible, timeAxis, timestep );
		final S spatialPixels = spatialPixelGenerator.getPaintPixels( hs, position, timestep, size );
		final Point p = new Point( accessible.numDimensions() );
		p.setPosition( timestep, timeAxis );
		final int[] backMapDims = new int[ p.numDimensions() - 1 ];
		for ( int i = 0; i < backMapDims.length; ++i )
			backMapDims[ i ] = i >= timeAxis ? i + 1 : i;
		return new IteratorOverBackMappingPoints<>( spatialPixels, p, backMapDims );
	}

}
