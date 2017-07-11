package net.imglib2.atlas;

import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedIntType;

public class LabelLoader implements CellLoader< UnsignedIntType >
{
	private final CellGrid grid;

	private final int background;

	public LabelLoader( final CellGrid grid, final int background )
	{
		this.grid = grid;

		this.background = background;
	}

	@Override
	public void load( final Img< UnsignedIntType > img ) throws Exception
	{
		img.forEach( pixel -> pixel.set( background ) );

	}
}
