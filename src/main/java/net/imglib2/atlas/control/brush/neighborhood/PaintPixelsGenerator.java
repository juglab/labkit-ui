package net.imglib2.atlas.control.brush.neighborhood;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;

public interface PaintPixelsGenerator< T, S extends Iterator< T > & Localizable >
{

	public S getPaintPixels( RandomAccessible< T > accessible, RealLocalizable position, int timestep, int size );

}
