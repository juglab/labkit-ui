package net.imglib2.cache.exampleclassifier.train2;

import net.imglib2.cache.CacheLoader;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class FeatureGeneratorLoader< S extends RealType< S > > implements CacheLoader< Long, Cell< VolatileFloatArray > >
{
	private final CellGrid grid;

	private final FeatureGenerator< S, FloatType > generator;

	public FeatureGeneratorLoader(
			final CellGrid grid,
			final FeatureGenerator< S, FloatType > generator )
	{
		this.grid = grid;
		this.generator = generator;
	}

	@Override
	public Cell< VolatileFloatArray > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );

		final FeatureGenerator< S, FloatType > generator = this.generator.copy();

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final VolatileFloatArray array = new VolatileFloatArray( blocksize, true );

		final Img< FloatType > img = ArrayImgs.floats( array.getCurrentStorageArray(), Util.int2long( cellDims ) );
		generator.generateFeatures( Views.translate( img, cellMin ) );

		return new Cell<>( cellDims, cellMin, array );
	}
}