package net.imglib2.atlas;

import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.IntegerType;

public class LabelLoader< T extends IntegerType< T > > implements CellLoader< T >
{
	private final CellGrid grid;

	private final int background;

	public LabelLoader( final CellGrid grid, final int background )
	{
		this.grid = grid;

		this.background = background;
	}

	@Override
	public void load( final Img< T > img ) throws Exception
	{
		img.forEach( pixel -> pixel.setInteger( background ) );

	}
}
